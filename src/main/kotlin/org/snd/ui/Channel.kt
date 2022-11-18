package org.snd.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import mu.KotlinLogging
import org.snd.cytube.CytubeClient
import org.snd.cytube.CytubeEventHandler
import org.snd.image.ImageLoader
import org.snd.ui.chat.Chat
import org.snd.ui.playlist.Playlist
import org.snd.ui.poll.Poll
import org.snd.ui.poll.PollState
import org.snd.ui.settings.SettingsModel
import org.snd.ui.videoplayer.VideoPlayerState

private val logger = KotlinLogging.logger {}

class Channel(
    val connectionStatus: ConnectionStatus,
    val settings: SettingsModel,
    val cytube: CytubeClient,
    imageLoader: ImageLoader,
) {
    val poll = PollState(cytube)
    val player = VideoPlayerState(settings)
    val playlist = Playlist(cytube)
    val chat: Chat = Chat(cytube, settings, connectionStatus, imageLoader, poll)

    fun disconnected() {
        connectionStatus.disconnect()
        if (!connectionStatus.hasConnectedBefore)
            reset()
    }

    fun connectionError() {
        connectionStatus.disconnect("Can't connect to the server")
    }

    fun newPoll(poll: Poll) {
        this.poll.startNewPoll(poll)
        chat.addMessage(Chat.Message.AnnouncementMessage("${poll.initiator} opened a poll: ${poll.title}"))
    }

    fun kicked(reason: String) {
        connectionStatus.disconnect(reason)
    }

    private fun reset() {
        chat.reset()
        player.mrl = null
        playlist.reset()
    }

    suspend fun init() {
        cytube.eventHandler = CytubeEventHandler(this)
        connect()
    }

    suspend fun reconnect() {
        val channel = settings.channel
        if (channel != null && connectionStatus.currentChannel == null && connectionStatus.hasConnectedBefore) {
            try {
                cytube.joinChannel(channel)
                connectionStatus.currentChannel = channel
            } catch (e: Exception) {
                logger.error(e) { }
                settings.channel = null
            }
            login()
        }
    }

    private suspend fun connect() {
        val channel = settings.channel
        if (channel != null) {
            try {
                cytube.connect(channel)
                connectionStatus.currentChannel = channel
            } catch (e: Exception) {
                logger.error(e) { }
                settings.channel = null
            }
            login()
        }
    }

    private suspend fun login() {
        val username = settings.username
        val password = username?.let { settings.settingsRepository.loadPassword(it) }
        if (username != null && password != null) {
            try {
                cytube.login(username, password)
            } catch (e: Exception) {
                logger.error(e) { }
                settings.username = null
            }
        }
    }
}

class ConnectionStatus {
    var currentUser by mutableStateOf<String?>(null)
    var currentChannel by mutableStateOf<String?>(null)
    var isGuest by mutableStateOf(false)
    var hasConnectedBefore by mutableStateOf(false)
    var disconnectReason by mutableStateOf<String?>(null)

    fun connectedAndAuthenticated() = currentUser != null && currentChannel != null

    fun disconnect(disconnectReason: String? = null) {
        currentUser = null
        currentChannel = null
        this.disconnectReason = disconnectReason
    }
}