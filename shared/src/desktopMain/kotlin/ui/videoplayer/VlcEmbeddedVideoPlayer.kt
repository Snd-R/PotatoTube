package ui.videoplayer

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter
import uk.co.caprica.vlcj.player.component.CallbackMediaPlayerComponent
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent
import uk.co.caprica.vlcj.player.component.InputEvents
import java.awt.Component
import java.awt.event.MouseWheelEvent
import java.awt.event.MouseWheelListener
import java.util.*

@Composable
fun VlcEmbeddedVideoPlayer(
    state: VideoPlayerState,
    modifier: Modifier,
    scrollState: ScrollState
) {
    val coroutineScope = rememberCoroutineScope()
    val mediaPlayerComponent = remember { initializeMediaPlayerComponent(state, coroutineScope, scrollState) }
    val mediaPlayer = remember { mediaPlayerComponent.mediaPlayer() }

    val factory = remember { { mediaPlayerComponent } }

    val mrl by state.mrl.collectAsState()
    val isPlaying by state.isPlaying.collectAsState()
    val isMuted by state.isMuted.collectAsState()
    val volume by state.volume.collectAsState()
    val updatedExternally by state.timeState.updatedExternallyToggle.collectAsState()

    LaunchedEffect(mrl) { mediaPlayer.media().play(mrl) }

    LaunchedEffect(updatedExternally) {
        withContext(Dispatchers.IO) {
            launch { mediaPlayer.controls().setTime(state.timeState.time.value) }
        }
    }

    LaunchedEffect(isPlaying) {
        withContext(Dispatchers.IO) {
            launch {
                if (isPlaying) {
                    mediaPlayer.controls().start()
                } else {
                    mediaPlayer.controls().pause()
                }
            }
        }
    }
    LaunchedEffect(volume) {
        withContext(Dispatchers.IO) {
            launch { mediaPlayer.audio().setVolume(volume) }
        }
    }

    LaunchedEffect(isMuted) {
        withContext(Dispatchers.IO) {
            launch { mediaPlayer.audio().isMute = isMuted }
        }
    }

    DisposableEffect(Unit) { onDispose { mediaPlayer.release() } }

    SwingPanel(
        factory = factory,
        background = Color.Transparent,
        modifier = modifier.then(Modifier.aspectRatio(1.735f))
    )
}

private fun initializeMediaPlayerComponent(
    state: VideoPlayerState,
    coroutineScope: CoroutineScope,
    scrollState: ScrollState
): Component {
    NativeDiscovery().discover()
    val component = if (isMacOS()) {
        CallbackMediaPlayerComponent(null, null, InputEvents.NONE, true, null, null, null, null)
    } else {
        EmbeddedMediaPlayerComponent(null, null, null, InputEvents.NONE, null)
    }

    component.mediaPlayer().events().addMediaPlayerEventListener(VlcEventListener(state))
    (component as Component).addMouseWheelListener(VlcScrollStateListener(scrollState, coroutineScope))

    return component
}

private fun Component.mediaPlayer() = when (this) {
    is CallbackMediaPlayerComponent -> mediaPlayer()
    is EmbeddedMediaPlayerComponent -> mediaPlayer()
    else -> error("mediaPlayer() can only be called on vlcj player components")
}

private fun isMacOS(): Boolean {
    val os = System
        .getProperty("os.name", "generic")
        .lowercase(Locale.ENGLISH)
    return "mac" in os || "darwin" in os
}

private class VlcEventListener(private val state: VideoPlayerState) : MediaPlayerEventAdapter() {
    override fun mediaPlayerReady(mediaPlayer: MediaPlayer) {
        state.setLength(mediaPlayer.status().length())
        state.play()
    }

    override fun finished(mediaPlayer: MediaPlayer) {
        state.pause()
        state.setMrl(null)
        state.timeState.updateInternally(0L)
    }

    override fun timeChanged(mediaPlayer: MediaPlayer, newTime: Long) {
        state.timeState.updateInternally(newTime)
    }

    override fun buffering(mediaPlayer: MediaPlayer?, newCache: Float) {
        state.setBuffering(newCache != 100.0f)
    }
}

private class VlcScrollStateListener(
    private val scrollState: ScrollState,
    private val coroutineScope: CoroutineScope,
) : MouseWheelListener {
    override fun mouseWheelMoved(event: MouseWheelEvent) {
        val scrollAmount = when {
            event.wheelRotation < 0 -> event.scrollAmount.toFloat() * -10
            else -> event.scrollAmount.toFloat() * 10
        }
        if (!scrollState.isScrollInProgress) {
            coroutineScope.launch { scrollState.scrollBy(scrollAmount) }
        }
    }

}