package ui.channel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cytube.CytubeClient
import cytube.CytubeEventHandler
import image.ImageLoader
import mu.KotlinLogging
import ui.chat.ChatState
import ui.chat.ChatState.Message.AnnouncementMessage
import ui.chat.ChatState.Message.ConnectionMessage
import ui.chat.ChatState.Message.ConnectionMessage.ConnectionType.CONNECTED
import ui.chat.ChatState.Message.ConnectionMessage.ConnectionType.DISCONNECTED
import ui.playlist.PlaylistState
import ui.poll.Poll
import ui.poll.PollState
import ui.settings.SettingsState
import ui.videoplayer.VideoPlayerState

private val logger = KotlinLogging.logger {}

class ChannelState(
    val connectionStatus: ConnectionStatus,
    val settings: SettingsState,
    val cytube: CytubeClient,
    imageLoader: ImageLoader,
) {
    val poll = PollState(cytube)
    val player = VideoPlayerState(settings)
    val playlist = PlaylistState(cytube)
    val chat: ChatState = ChatState(cytube, settings, connectionStatus, imageLoader, poll)

    private var isInitialized by mutableStateOf(false)

    fun joinedChannel(channel: String) {
        connectionStatus.hasConnectedBefore = true
        connectionStatus.currentChannel = channel
        connectionStatus.disconnectReason = null
        chat.addConnectionMessage(ConnectionMessage("Connected", CONNECTED))
    }

    fun disconnected() {
        connectionStatus.disconnect()
        if (!connectionStatus.hasConnectedBefore)
            reset()
        else if (!connectionStatus.kicked)
            chat.addConnectionMessage(ConnectionMessage("Disconnected", DISCONNECTED))
    }

    fun connectionError() {
        connectionStatus.disconnect("Can't connect to the server")
    }

    fun newPoll(poll: Poll) {
        this.poll.startNewPoll(poll)
        chat.addAnnouncementMessage(AnnouncementMessage("${poll.initiator} opened a poll: ${poll.title}"))
    }

    fun kicked(reason: String) {
        connectionStatus.kicked = true
        chat.addConnectionMessage(ConnectionMessage("Kicked: $reason", DISCONNECTED))
        connectionStatus.disconnect()
    }

    private fun reset() {
        chat.reset()
        player.setMrl(null)
        playlist.reset()
    }

    suspend fun init() {
        if (isInitialized) return
        this.isInitialized = true

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
    var kicked by mutableStateOf(false)
    var disconnectReason by mutableStateOf<String?>(null)

    fun connectedAndAuthenticated() = currentUser != null && currentChannel != null

    fun disconnect(disconnectReason: String? = null) {
        currentUser = null
        currentChannel = null
        this.disconnectReason = disconnectReason
    }
}