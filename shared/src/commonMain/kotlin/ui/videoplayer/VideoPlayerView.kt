package ui.videoplayer


import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun VideoPlayerView(state: VideoPlayerState, modifier: Modifier)
