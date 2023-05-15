package ui.videoplayer

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import mpv.MpvEmbeddedPlayerComponent

@Composable
fun MpvEmbeddedVideoPlayer(
    state: VideoPlayerState,
    modifier: Modifier,
    scrollState: ScrollState
) {
    val coroutineScope = rememberCoroutineScope()
    val mediaPlayerComponent = remember {
        MpvEmbeddedPlayerComponent(scrollHandler = { scrollAmount: Float ->
            coroutineScope.launch { if (!scrollState.isScrollInProgress) scrollState.scrollBy(scrollAmount) }
        })
    }

    val mediaPlayer = remember { mediaPlayerComponent.mediaPlayer }
    mediaPlayer.setOnVideoLoadedCallback { player -> state.setLength(player.duration()) }

    val factory = remember { { mediaPlayerComponent } }

    val mrl by state.mrl.collectAsState()
    val updatedExternally by state.timeState.updatedExternallyToggle.collectAsState()
    val isPlaying by state.isPlaying.collectAsState()
    val volume by state.volume.collectAsState()
    val isMuted by state.isMuted.collectAsState()

    LaunchedEffect(mrl) { mrl?.let { mediaPlayer.play(it) } }
    LaunchedEffect(updatedExternally) { mediaPlayer.seek(state.timeState.time.value) }
    LaunchedEffect(isPlaying) {
        if (isPlaying) mediaPlayer.unPause()
        else mediaPlayer.pause()
    }

    LaunchedEffect(volume) { mediaPlayer.setVolume(volume) }
    LaunchedEffect(isMuted) {
        if (isMuted) mediaPlayer.mute()
        else mediaPlayer.unMute()
    }
    LaunchedEffect(mrl) {
        while (isActive) {
            state.timeState.updateInternally(mediaPlayer.currentPosition())
            delay(250)
        }
    }

    SwingPanel(
        factory = factory,
        background = Color.Transparent,
        modifier = modifier.then(Modifier.aspectRatio(1.735f))
    )

    DisposableEffect(Unit) { onDispose { mediaPlayer.release() } }
}
