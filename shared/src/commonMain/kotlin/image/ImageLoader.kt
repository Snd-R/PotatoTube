package image

import androidx.compose.runtime.Composable
import ui.chat.ChatState

interface ImageLoader {
    @Composable
    fun LoadEmoteImage(
        emote: ChatState.Emote,
        emoteDimensions: ChatState.EmoteDimensions,
        maxHeight:Int?,
        maxWidth:Int?,
    )
}