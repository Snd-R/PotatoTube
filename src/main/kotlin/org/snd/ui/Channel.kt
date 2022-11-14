package org.snd.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.snd.cytube.CytubeClient
import org.snd.cytube.CytubeEventHandler
import org.snd.image.ImageLoader
import org.snd.ui.chat.Chat
import org.snd.ui.playlist.Playlist
import org.snd.ui.settings.SettingsModel
import org.snd.ui.videoplayer.VideoPlayerState

class Channel(
    val userStatus: UserStatus,
    val settings: SettingsModel,
    val cytube: CytubeClient,
    imageLoader: ImageLoader
) {
    val chat: Chat = Chat(cytube, settings, userStatus, imageLoader)
    val player = VideoPlayerState(settings)
    val playlist = Playlist(cytube)

    fun connected() {
    }

    fun disconnected() {
        userStatus.disconnect()
        reset()
    }

    fun connectionError() {
        userStatus.disconnect("Can't connect to the server")
    }

    fun kicked(reason: String) {
        userStatus.disconnect(reason)
    }

    private fun reset() {
        chat.reset()
        player.mrl = null
        playlist.reset()
    }

    suspend fun connect() {
        cytube.connect()
        cytube.registerEventHandler(CytubeEventHandler(this))

        val channel = settings.channel
        if (channel != null) {
            try {
                cytube.joinChannel(channel)
                userStatus.currentChannel = channel
            } catch (e: Exception) {
                settings.channel = null
            }
        }

        val username = settings.username
        val password = username?.let { settings.settingsRepository.loadPassword(it) }
        if (username != null && password != null) {
            try {
                cytube.login(username, password)
            } catch (e: Exception) {
                settings.username = null
            }
        }
    }
}

class UserStatus {
    var currentUser by mutableStateOf<String?>(null)
    var currentChannel by mutableStateOf<String?>(null)
    var isGuest by mutableStateOf(false)
    var disconnectReason by mutableStateOf<String?>(null)

    fun connectedAndAuthenticated() = currentUser != null && currentChannel != null

    fun disconnect(disconnectReason: String? = null) {
        currentUser = null
        currentChannel = null
        this.disconnectReason = disconnectReason
    }
}