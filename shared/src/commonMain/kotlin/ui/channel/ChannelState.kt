package ui.channel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cytube.CytubeClient
import image.ImageLoader
import mu.KotlinLogging
import ui.ConnectionStatus
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

    fun joinedChannel() {
        chat.addConnectionMessage(ConnectionMessage("Connected", CONNECTED))
    }

    fun disconnected() {
        if (settings.channel == null)
            reset()
        else if (!connectionStatus.kicked && isInitialized)
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
        chat.addConnectionMessage(ConnectionMessage("Kicked: $reason", DISCONNECTED))
    }

    fun disconnect() {
        settings.channel = null
        cytube.disconnect()
    }

    private fun reset() {
        chat.reset()
        player.setMrl(null)
        playlist.reset()
    }

    suspend fun init() {
        if (connectionStatus.currentChannel == settings.channel
            && connectionStatus.currentUser == settings.username
        ) return

        isInitialized = false
        chat.isLoading = true
        reset()
        connect()
        isInitialized = true
        chat.isLoading = false
    }

    suspend fun reconnect() {
        val channel = settings.channel
        if (isInitialized && channel != null && connectionStatus.currentChannel == null && !connectionStatus.kicked) {
            try {
                cytube.joinChannel(channel)
            } catch (e: Exception) {
                logger.error(e) { }
                connectionStatus.disconnect(e.message)
            }
            login()
        }
    }

    private suspend fun connect() {
        val channel = settings.channel
        if (channel != null) {
            try {
                cytube.connectToChannel(channel)
            } catch (e: Exception) {
                logger.error(e) { }
                connectionStatus.disconnect(e.message)
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
