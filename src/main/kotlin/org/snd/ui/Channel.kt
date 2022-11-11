package org.snd.ui

import org.snd.cytube.CytubeClient
import org.snd.cytube.CytubeEventHandler
import org.snd.image.ImageLoader
import org.snd.ui.chat.Chat
import org.snd.ui.playlist.Playlist
import org.snd.ui.settings.SettingsModel
import org.snd.ui.videoplayer.VideoPlayerState

class Channel(
    val settings: SettingsModel,
    val cytube: CytubeClient,
    imageLoader: ImageLoader
) {
    val chat: Chat = Chat(cytube, settings, imageLoader)
    val player = VideoPlayerState(settings)
    val playlist = Playlist(cytube)

    fun connected() {
        chat.connected = true
    }

    fun disconnected() {
        chat.connected = false
        reset()
    }

    fun connectionError() {
        chat.connected = false
        chat.connectionErrorReason = "Can't connect to the server"
    }

    fun kicked(reason: String) {
        chat.connected = false
        chat.connectionErrorReason = reason
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
            } catch (e: Exception) {
                settings.channel = null
            }
        }

        val username = settings.username
        val password = settings.password
        if (username != null && password != null) {
            try {
                cytube.login(username, password)
            } catch (e: Exception) {
                settings.username = null
                settings.password = null
            }
        }
    }
}