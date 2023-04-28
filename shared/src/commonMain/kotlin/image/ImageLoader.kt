package image

import androidx.compose.runtime.Composable
import ui.chat.Chat

interface ImageLoader {
    @Composable
    fun LoadEmoteImage(
        emote: Chat.Emote,
        dimension: Chat.EmoteDimension,
        scaleTo: Dimension?,
    )
}