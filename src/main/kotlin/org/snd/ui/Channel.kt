package org.snd.ui

import org.snd.cytube.CytubeClient
import org.snd.image.ImageLoader
import org.snd.settings.CytubeState
import org.snd.ui.chat.Chat
import org.snd.ui.settings.SettingsModel
import org.snd.ui.videoplayer.VideoPlayerState

class Channel(
    val settings: SettingsModel,
    val cytube: CytubeClient,
    cytubeState: CytubeState,
    imageLoader: ImageLoader
) {
    val chat: Chat = Chat(cytube, cytubeState, settings, imageLoader)
    val player = VideoPlayerState()

    fun disconnect() {
        chat.reset()
    }
}