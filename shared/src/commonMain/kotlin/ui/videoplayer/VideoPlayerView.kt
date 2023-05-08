package ui.videoplayer


import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import player.PlayerType

@Composable
expect fun VideoPlayerView(
    state: VideoPlayerState,
    type: PlayerType,
    modifier: Modifier,
    scrollState: ScrollState
)
