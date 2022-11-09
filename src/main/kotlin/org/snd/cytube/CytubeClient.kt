package org.snd.cytube

import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import okhttp3.OkHttpClient
import org.jetbrains.compose.resources.LoadState
import org.json.JSONArray
import org.json.JSONObject
import org.snd.settings.CytubeState
import org.snd.ui.Channel
import org.snd.ui.chat.Chat
import java.time.Instant
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val CYTUBE_URL = "https://bigapple.cytu.be:8443" // TODO resolve actual partition on connect

private val logger = KotlinLogging.logger {}

class CytubeClient(
    private val okHttpClient: OkHttpClient,
    private val cytubeState: CytubeState,
    private val coroutineScope: CoroutineScope
) {
    private var socket: Socket? = null

    fun start(channel: Channel) {
        socket?.close()

        val options: IO.Options = IO.Options.builder().build()
        options.callFactory = okHttpClient
        options.webSocketFactory = okHttpClient

        val socket = IO.socket(CYTUBE_URL)
        registerHandlers(socket, channel)
        this.socket = socket

        socket.onAnyIncoming { logger.debug { "incoming ${it.joinToString()}" } }
        socket.onAnyOutgoing() { logger.debug { "incoming ${it.joinToString()}" } }
        socket.connect()

        cytubeState.currentChannel = channel.settings.channel
        socket.emit("joinChannel", JSONObject().put("name", channel.settings.channel))
        val username = channel.settings.username
        val password = channel.settings.password

        if (username != null && password != null) {
            socket?.emit("login", JSONObject().put("name", username).put("pw", password))
        }
    }

    fun logout() {
        val currentChannel = cytubeState.currentChannel
        socket?.disconnect()
        socket?.connect()
        socket?.emit("joinChannel", JSONObject().put("name", currentChannel))
    }

    fun disconnectFromChannel() {
        socket?.close()
        cytubeState.authenticated = false
        socket?.connect()
    }

    fun changeChannel(channelName: String) {
        disconnectFromChannel()
        socket?.emit("joinChannel", JSONObject().put("name", channelName))
    }

    fun sendMessage(message: String) {
        socket?.emit(
            "chatMsg", JSONObject()
                .put("msg", message)
                .put("meta", emptyMap<String, String>())
        )
    }

    fun anonymousLogin(username: String) {
        socket?.emit("login", JSONObject().put("name", username))
    }

    suspend fun login(username: String, password: String): LoadState<String> {
        return suspendCoroutine { continuation ->
            socket?.once("login") {
                val response = it[0] as JSONObject
                if (response.getBoolean("success")) {
                    val name = response.getString("name")
                    continuation.resume(LoadState.Success(name))
                } else {
                    val error = response.getString("error")
                    continuation.resume(LoadState.Error(RuntimeException(error)))
                }
            }

            socket?.emit("login", JSONObject().put("name", username).put("pw", password))
        }
    }

    private fun registerHandlers(socket: Socket, channel: Channel) {
        socket.on("chatMsg") { args ->
            val response = args[0] as JSONObject

            coroutineScope.launch {
                channel.chat.addMessage(
                    Chat.Message(
                        timestamp = Instant.ofEpochMilli(response.getLong("time")),
                        user = response.getString("username"),
                        message = response.getString("msg")
                    )
                )
            }
        }

        socket.on("login") {
            val response = it[0] as JSONObject
            if (response.getBoolean("success")) {
                coroutineScope.launch {
                    cytubeState.authenticated = true
                    cytubeState.username = response.getString("name")
                }
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
            coroutineScope.launch {
                channel.chat.setEmotes(emotes)
            }
        }

        socket.on("connect") {
        }
        socket.on("connect_error") {
        }
        socket.on("disconnect") {
            channel.disconnect()
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
            coroutineScope.launch {
                channel.chat.users.setUsers(users)
            }
        }

        socket.on("usercount") {
            val response = it[0] as Int

            coroutineScope.launch {
                channel.chat.users.userCount(response)
            }
        }

        socket.on("setAFK") {
            val response = it[0] as JSONObject
            val afk = response.getBoolean("afk")
            val name = response.getString("name")

            coroutineScope.launch {
                channel.chat.users.setAfk(name, afk)
            }
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
            coroutineScope.launch {
                channel.chat.users.addUser(user)
            }
        }

        socket.on("userLeave") {
            val response = it[0] as JSONObject
            val name = response.getString("name")

            coroutineScope.launch {
                channel.chat.users.removeUser(name)
            }
        }

        socket.on("joinChannel") {
            val response = it[0] as JSONObject
            val name = response.getString("name")

            coroutineScope.launch {
                cytubeState.currentChannel = name
            }
        }

        socket.on("changeMedia") {
            val response = it[0] as JSONObject
            val type = response.getString("type")
            val id = response.getString("id")

            val mrl = if (type == "yt") {
                "https://www.youtube.com/watch?v=$id"
            } else id
            coroutineScope.launch {
                channel.player.mrl = mrl
            }
        }
    }
}