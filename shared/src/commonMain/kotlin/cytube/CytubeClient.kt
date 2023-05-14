package cytube

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter.Listener
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import mu.KotlinLogging
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.apache.commons.text.StringEscapeUtils.unescapeHtml4
import org.jetbrains.compose.resources.LoadState
import org.json.JSONArray
import org.json.JSONObject
import ui.chat.ChatState
import ui.chat.ChatState.Message.UserMessage
import ui.playlist.MediaItem
import ui.playlist.PlaylistItem
import ui.poll.Poll
import ui.poll.PollOption
import java.io.IOException
import java.time.Instant
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

const val CYTUBE_BASE_URL = "https://cytu.be"
const val CYTUBE_DEFAULT_PARTITION = "https://bigapple.cytu.be:8443"
const val YOUTUBE_PREFIX = "https://www.youtube.com/watch?v="

private val logger = KotlinLogging.logger {}

class CytubeClient(
    private val httpClient: OkHttpClient,
    private val moshi: Moshi
) {
    private val eventListeners: MutableList<CytubeEventHandler> = CopyOnWriteArrayList()
    private val timeout = 10.seconds

    @Volatile
    private var socket: Socket? = null

    fun addEventListener(listener: CytubeEventHandler) {
        this.eventListeners.add(listener)
    }

    suspend fun connectToChannel(channel: String) {
        val partition = getPartition(channel)
        connect(partition)
        joinChannel(channel)
    }

    fun connectToDefaultPartition() {
        connect(CYTUBE_DEFAULT_PARTITION)
    }

    private fun connect(partition: String) {
        socket?.close()
        val options: IO.Options = IO.Options.builder().build()
            .apply {
                callFactory = httpClient
                webSocketFactory = httpClient
            }

        val socket = IO.socket(partition, options)
        this.socket = socket

        socket.onAnyIncoming { logger.info { "incoming ${it.joinToString()}" } }
        socket.onAnyOutgoing { logger.info { "outgoing ${it.joinToString()}" } }
        registerEventHandler(socket)
        socket.connect()
    }

    fun disconnect() {
        eventListeners.forEach { it.onUserInitiatedDisconnect() }
        socket?.close()
    }

    private suspend fun getPartition(channel: String): String {
        val request = Request.Builder()
            .url(
                CYTUBE_BASE_URL.toHttpUrl().newBuilder()
                    .addPathSegments("socketconfig/$channel.json")
                    .build()
            ).build()

        val json = suspendCancellableCoroutine { continuation ->
            httpClient.newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string()
                    if (body == null) continuation.resumeWithException(RuntimeException("empty body"))
                    else continuation.resume(body)
                }

                override fun onFailure(call: Call, e: IOException) {
                    if (continuation.isCancelled) return
                    continuation.resumeWithException(e)
                }
            })
        }

        val socketConfig = moshi.adapter<SocketConfig>().fromJson(json)
            ?: throw RuntimeException("can't parse json")

        return socketConfig.servers.firstOrNull { it.secure }?.url
            ?: socketConfig.servers.first().url

    }

    suspend fun joinChannel(channelName: String) {
        return suspendCoroutine { continuation ->
            val errorCallback = object : Listener {
                override fun call(vararg args: Any?) {
                    val response = args[0] as JSONObject
                    val message = response.getString("msg")
                    if (message.startsWith("Invalid channel name")) {
                        socket?.off("errorMsg", this)
                        continuation.resumeWithException(RuntimeException(message))
                    }
                }
            }

            socket?.on("errorMsg", errorCallback)
            socket?.once("setPermissions") {
                eventListeners.forEach { listener -> listener.onChannelJoin(channelName) }
                continuation.resume(Unit)
            }
            socket?.emit("joinChannel", JSONObject().put("name", channelName))
        }
    }

    fun sendMessage(message: String) {
        socket?.emit(
            "chatMsg", JSONObject()
                .put("msg", message)
                .put("meta", JSONObject())
        )
    }

    suspend fun login(username: String, password: String?): String {
        return suspendCoroutineWithTimeout(timeout) { continuation ->
            socket?.once("login") {
                val response = it[0] as JSONObject
                if (response.getBoolean("success")) {
                    val name = response.getString("name")
                    continuation.resume(name)
                } else {
                    val error = response.getString("error")
                    continuation.resumeWithException(RuntimeException(error))
                }
            }

            socket?.emit("login", JSONObject().put("name", username).putOpt("pw", password))
        }
    }

    suspend fun queue(url: String, putLast: Boolean, temp: Boolean): LoadState<Unit> {
        return suspendCoroutineWithTimeout(timeout) { continuation ->
            val type = if (url.startsWith(YOUTUBE_PREFIX)) "yt" else "fi"
            val submittedId = if (type == "yt") url.removePrefix(YOUTUBE_PREFIX) else url

            val successCallback: Listener?
            var failureCallback: Listener? = null
            successCallback = object : Listener {
                override fun call(vararg args: Any?) {
                    val response = args[0] as JSONObject
                    val item = parsePlaylistItem(response.getJSONObject("item"))
                    if (item.media.id == submittedId) {
                        socket?.off("queue", this)
                        socket?.off("queueFail", failureCallback)
                        continuation.resume(LoadState.Success(Unit))
                    }
                }
            }
            failureCallback = object : Listener {
                override fun call(vararg args: Any?) {
                    val response = args[0] as JSONObject
                    val id = response.getString("id")
                    if (id == submittedId) {
                        val msg = response.getString("msg")
                        socket?.off("queueFail", this)
                        socket?.off("queue", successCallback)
                        continuation.resume(LoadState.Error(RuntimeException(msg)))
                    }
                }
            }

            socket?.on("queue", successCallback)
            socket?.on("queueFail", failureCallback)
            socket?.emit(
                "queue", JSONObject()
                    .put("id", submittedId)
                    .put("type", type)
                    .put("pos", if (putLast) "end" else "next")
                    .put("temp", temp)
            )
        }
    }

    fun pollVote(optionNumber: Int) {
        socket?.emit(
            "vote", JSONObject()
                .put("option", optionNumber)
        )
    }

    private fun registerEventHandler(socket: Socket) {
        socket.on("chatMsg") { args ->
            val response = args[0] as JSONObject
            eventListeners.forEach { listener ->
                listener.onChatMessage(
                    UserMessage(
                        timestamp = Instant.ofEpochMilli(response.getLong("time")),
                        user = response.getString("username"),
                        message = response.getString("msg")
                    )
                )
            }
        }

        socket.on("login") {
            val response = it[0] as JSONObject
            val success = response.getBoolean("success")
            if (success) {
                val name = response.getString("name")
                val isGuest = response.optBoolean("guest")
                eventListeners.forEach { listener -> listener.onLoginSuccess(name, isGuest) }
            }
        }

        socket.on("emoteList") {
            val response: JSONArray = it[0] as JSONArray
            val emotes = mutableListOf<ChatState.Emote>()
            for (i in 0 until response.length()) {
                val emote = response.getJSONObject(i)
                val name = emote.getString("name")
                val image = emote.getString("image")
                emotes.add(ChatState.Emote(name, image))
            }
            eventListeners.forEach { listener -> listener.onEmoteList(emotes) }
        }

        socket.on("userlist") {
            val response: JSONArray = it[0] as JSONArray
            val users = mutableListOf<ChatState.User>()
            for (i in 0 until response.length()) {
                val user = response.getJSONObject(i)
                val meta = user.getJSONObject("meta")
                users.add(
                    ChatState.User(
                        name = user.getString("name"),
                        rank = user.getDouble("rank"),
                        afk = meta.getBoolean("afk"),
                        muted = meta.getBoolean("muted")
                    )
                )
            }
            eventListeners.forEach { listener -> listener.onUserList(users) }
        }

        socket.on("usercount") {
            val userCount = it[0] as Int
            eventListeners.forEach { listener -> listener.onUserCount(userCount) }
        }

        socket.on("setAFK") {
            val response = it[0] as JSONObject
            val afk = response.getBoolean("afk")
            val name = response.getString("name")
            eventListeners.forEach { listener -> listener.onSetAfk(name, afk) }
        }

        socket.on("addUser") {
            val response = it[0] as JSONObject
            val meta = response.getJSONObject("meta")
            val user = ChatState.User(
                name = response.getString("name"),
                rank = response.getDouble("rank"),
                afk = meta.getBoolean("afk"),
                muted = meta.getBoolean("muted")
            )
            eventListeners.forEach { listener -> listener.onAddUser(user) }
        }

        socket.on("userLeave") {
            val response = it[0] as JSONObject
            val name = response.getString("name")
            eventListeners.forEach { listener -> listener.onUserLeave(name) }
        }

        socket.on("changeMedia") {
            val response = it[0] as JSONObject
            val type = response.getString("type")
            val id = response.getString("id")

            val mrl = if (type == "yt") {
                YOUTUBE_PREFIX + id
            } else id
            eventListeners.forEach { listener -> listener.onChangeMedia(mrl) }
        }

        socket.on("mediaUpdate") {
            val response = it[0] as JSONObject
            val currentTime = response.getDouble("currentTime")
            val paused = response.getBoolean("paused")
            val time = (currentTime * 1000).toLong()

            eventListeners.forEach { listener -> listener.onMediaUpdate(time, paused) }
        }

        socket.on("queue") {
            val response = it[0] as JSONObject
            val item = parsePlaylistItem(response.getJSONObject("item"))
            val after = response.optString("after")

            eventListeners.forEach { listener -> listener.onQueue(item, after) }
        }

        socket.on("playlist") {
            val response = it[0] as JSONArray
            val items = mutableListOf<PlaylistItem>()
            for (i in 0 until response.length()) {
                items.add(parsePlaylistItem(response.getJSONObject(i)))
            }
            eventListeners.forEach { listener -> listener.onPlaylist(items) }
        }

        socket.on("setPlaylistMeta") {
            val response = it[0] as JSONObject
            val rawTime = response.getLong("rawTime")
            val count = response.getInt("count")
            val time = response.getString("time")

            eventListeners.forEach { listener -> listener.onPlaylistMeta(rawTime, count, time) }
        }

        socket.on("delete") {
            val response = it[0] as JSONObject
            val uid = response.getInt("uid")
            eventListeners.forEach { listener -> listener.onDeletePlaylistItem(uid) }
        }

        socket.on("moveVideo") {
            val response = it[0] as JSONObject
            val from = response.getInt("from")
            val after = response.getString("after")
            if (after == "prepend")
                eventListeners.forEach { listener -> listener.onMoveVideoToStart(from) }
            else eventListeners.forEach { listener -> listener.onMoveVideo(from, after.toInt()) }
        }

        socket.on("setPlaylistLocked") {
            val locked = it[0] as Boolean
            eventListeners.forEach { listener -> listener.onPlaylistLock(locked) }
        }

        socket.on("connect") {
            logger.debug { "connected" }
            eventListeners.forEach { listener -> listener.onConnect() }
        }
        socket.on("connect_error") {
            logger.debug { "connection error" }
            eventListeners.forEach { listener -> listener.onConnectError() }
        }
        socket.on("disconnect") {
            logger.debug { "disconnected" }
            eventListeners.forEach { listener -> listener.onDisconnect() }
        }

        socket.on("kick") {
            val response = it[0] as JSONObject
            eventListeners.forEach { listener -> listener.onKick(response.getString("reason")) }
        }

        socket.on("newPoll") {
            val response = it[0] as JSONObject
            eventListeners.forEach { listener -> listener.onNewPoll(parsePoll(response)) }
        }
        socket.on("updatePoll") {
            val response = it[0] as JSONObject
            eventListeners.forEach { listener -> listener.updatePoll(parsePoll(response)) }
        }

        socket.on("closePoll") {
            eventListeners.forEach { listener -> listener.closePoll() }
        }
        socket.on("updateEmote") {
            val response = it[0] as JSONObject
            val name = response.getString("name")
            val image = response.getString("image")
            eventListeners.forEach { listener -> listener.onUpdateEmote(name, image) }
        }
        socket.on("removeEmote") {
            val response = it[0] as JSONObject
            val name = response.getString("name")
            val image = response.getString("image")
            eventListeners.forEach { listener -> listener.onRemoveEmote(name, image) }
        }
    }

    private fun parsePlaylistItem(json: JSONObject): PlaylistItem {
        val media = json.getJSONObject("media")
        val mediaItem = MediaItem(
            duration = media.getString("duration"),
            seconds = media.getLong("seconds"),
            id = media.getString("id"),
            title = media.getString("title"),
            type = media.getString("type")
        )
        return PlaylistItem(
            uid = json.getInt("uid"),
            temp = json.getBoolean("temp"),
            queueBy = json.getString("queueby"),
            media = mediaItem
        )
    }

    private fun parsePoll(json: JSONObject): Poll {

        val countsJson = json.getJSONArray("counts")
        val counts = mutableListOf<Int>()
        for (i in 0 until countsJson.length())
            counts.add(countsJson.getInt(i))

        val optionsJson = json.getJSONArray("options")
        val options = mutableListOf<String>()
        for (i in 0 until optionsJson.length())
            options.add(optionsJson.getString(i))

        val initiator = json.getString("initiator")
        val title = unescapeHtml4(json.getString("title"))
        val timestamp = json.getLong("timestamp")

        return Poll(
            title = title,
            totalCount = counts.sum(),
            options = options.zip(counts)
                .mapIndexed { index, (option, count) -> PollOption(unescapeHtml4(option), count, index) },
            timestamp = Instant.ofEpochMilli(timestamp),
            initiator = initiator
        )
    }
}

suspend inline fun <T> suspendCoroutineWithTimeout(timeout: Duration, crossinline block: (Continuation<T>) -> Unit): T {
    var finalValue: T
    withTimeout(timeout) {
        finalValue = suspendCancellableCoroutine(block = block)
    }
    return finalValue
}