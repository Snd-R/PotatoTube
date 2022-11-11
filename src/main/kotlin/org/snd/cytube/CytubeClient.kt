package org.snd.cytube

import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter.Listener
import mu.KotlinLogging
import okhttp3.OkHttpClient
import org.jetbrains.compose.resources.LoadState
import org.json.JSONArray
import org.json.JSONObject
import org.snd.ui.chat.Chat
import org.snd.ui.playlist.MediaItem
import org.snd.ui.playlist.PlaylistItem
import java.time.Instant
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

const val CYTUBE_URL = "https://bigapple.cytu.be:8443" // TODO resolve actual partition on connect
const val YOUTUBE_PREFIX = "https://www.youtube.com/watch?v="

private val logger = KotlinLogging.logger {}

class CytubeClient(
    private val okHttpClient: OkHttpClient,
) {
    private var socket: Socket? = null

    fun connect() {
        socket?.close()
        val options: IO.Options = IO.Options.builder().build()
        options.callFactory = okHttpClient
        options.webSocketFactory = okHttpClient

        val socket = IO.socket(CYTUBE_URL)
        this.socket = socket

        socket.onAnyIncoming { logger.debug { "incoming ${it.joinToString()}" } }
        socket.onAnyOutgoing { logger.debug { "outgoing ${it.joinToString()}" } }
        socket.connect()
    }

    fun disconnectFromChannel() {
        socket?.close()
        socket?.connect()
    }

    suspend fun joinChannel(channelName: String) {
        return suspendCoroutine { continuation ->
            disconnectFromChannel()

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
            socket?.once("setPermissions") { continuation.resume(Unit) }
            socket?.emit("joinChannel", JSONObject().put("name", channelName))
        }
    }

    fun sendMessage(message: String) {
        socket?.emit(
            "chatMsg", JSONObject()
                .put("msg", message)
                .put("meta", emptyMap<String, String>())
        )
    }

    suspend fun login(username: String, password: String?): String {
        return suspendCoroutine { continuation ->
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
        return suspendCoroutine { continuation ->
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

    fun registerEventHandler(eventHandler: CytubeEventHandler) {
        val socket = this.socket ?: return
        socket.off()

        socket.on("chatMsg") { args ->
            val response = args[0] as JSONObject
            eventHandler.onChatMessage(
                Chat.Message(
                    timestamp = Instant.ofEpochMilli(response.getLong("time")),
                    user = response.getString("username"),
                    message = response.getString("msg")
                )
            )
        }

        socket.on("login") {
            val response = it[0] as JSONObject
            val success = response.getBoolean("success")
            if (success) {
                eventHandler.onLogin()
            }
        }

        socket.on("emoteList") {
            val response: JSONArray = it[0] as JSONArray
            val emotes = mutableListOf<Chat.Emote>()
            for (i in 0 until response.length()) {
                val emote = response.getJSONObject(i)
                val name = emote.getString("name")
                val image = emote.getString("image")
                emotes.add(Chat.Emote(name, image))
            }
            eventHandler.onEmoteList(emotes)
        }

        socket.on("userlist") {
            val response: JSONArray = it[0] as JSONArray
            val users = mutableListOf<Chat.User>()
            for (i in 0 until response.length()) {
                val user = response.getJSONObject(i)
                val meta = user.getJSONObject("meta")
                users.add(
                    Chat.User(
                        name = user.getString("name"),
                        rank = user.getDouble("rank"),
                        afk = meta.getBoolean("afk"),
                        muted = meta.getBoolean("muted")
                    )
                )
            }
            eventHandler.onUserList(users)
        }

        socket.on("usercount") {
            val userCount = it[0] as Int
            eventHandler.onUserCount(userCount)
        }

        socket.on("setAFK") {
            val response = it[0] as JSONObject
            val afk = response.getBoolean("afk")
            val name = response.getString("name")
            eventHandler.onSetAfk(name, afk)
        }

        socket.on("addUser") {
            val response = it[0] as JSONObject
            val meta = response.getJSONObject("meta")
            val user = Chat.User(
                name = response.getString("name"),
                rank = response.getDouble("rank"),
                afk = meta.getBoolean("afk"),
                muted = meta.getBoolean("muted")
            )
            eventHandler.onAddUser(user)
        }

        socket.on("userLeave") {
            val response = it[0] as JSONObject
            val name = response.getString("name")
            eventHandler.onUserLeave(name)
        }

        socket.on("changeMedia") {
            val response = it[0] as JSONObject
            val type = response.getString("type")
            val id = response.getString("id")

            val mrl = if (type == "yt") {
                YOUTUBE_PREFIX + id
            } else id
            eventHandler.onChangeMedia(mrl)
        }

        socket.on("mediaUpdate") {
            val response = it[0] as JSONObject
            val currentTime = response.getDouble("currentTime")
            val paused = response.getBoolean("paused")
            val time = (currentTime * 1000).toLong()

            eventHandler.onMediaUpdate(time, paused)
        }

        socket.on("queue") {
            val response = it[0] as JSONObject
            val item = parsePlaylistItem(response.getJSONObject("item"))
            val after = response.getString("after")

            eventHandler.onQueue(item, after)
        }

        socket.on("playlist") {
            val response = it[0] as JSONArray
            val items = mutableListOf<PlaylistItem>()
            for (i in 0 until response.length()) {
                items.add(parsePlaylistItem(response.getJSONObject(i)))
            }
            eventHandler.onPlaylist(items)
        }

        socket.on("setPlaylistMeta") {
            val response = it[0] as JSONObject
            val rawTime = response.getLong("rawTime")
            val count = response.getInt("count")
            val time = response.getString("time")

            eventHandler.onPlaylistMeta(rawTime, count, time)
        }

        socket.on("delete") {
            val response = it[0] as JSONObject
            val uid = response.getInt("uid")
            eventHandler.onDeletePlaylistItem(uid)
        }

        socket.on("moveVideo") {
            val response = it[0] as JSONObject
            val from = response.getInt("from")
            val after = response.getString("after")
            if (after == "prepend")
                eventHandler.onMoveVideoToStart(from)
            else eventHandler.onMoveVideo(from, after.toInt())
        }

        socket.on("setPlaylistLocked") {
            val locked = it[0] as Boolean
            eventHandler.onPlaylistLock(locked)
        }

        socket.on("connect") {
            eventHandler.onConnect()
        }
        socket.on("connect_error") {
            eventHandler.onConnectError()
        }
        socket.on("disconnect") {
            eventHandler.onDisconnect()
        }

        socket.on("kick") {
            val response = it[0] as JSONObject
            eventHandler.onKick(response.getString("reason"))
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

}
