package ui.videoplayer

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter
import uk.co.caprica.vlcj.player.component.CallbackMediaPlayerComponent
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent
import java.awt.Component
import java.util.*

@Composable
fun VlcEmbeddedVideoPlayer(
    state: VideoPlayerState,
    modifier: Modifier,
) {
    val mediaPlayerComponent = remember { initializeMediaPlayerComponent(state) }
    val mediaPlayer = remember { mediaPlayerComponent.mediaPlayer() }

    val factory = remember { { mediaPlayerComponent } }

    val mrl by state.mrl.collectAsState()
    val updatedExternally by state.timeState.updatedExternallyToggle.collectAsState()
    val time by state.timeState.time.collectAsState()
    val isPlaying by state.isPlaying.collectAsState()
    val volume by state.volume.collectAsState()
    val isMuted by state.isMuted.collectAsState()

    LaunchedEffect(key1 = mrl) {
        launch { mediaPlayer.media().play(mrl) }
    }

    LaunchedEffect(updatedExternally) {
        withContext(Dispatchers.IO) {
            launch { mediaPlayer.controls().setTime(time) }
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

/**
 * See https://github.com/caprica/vlcj/issues/887#issuecomment-503288294
 * for why we're using CallbackMediaPlayerComponent for macOS.
 */
private fun initializeMediaPlayerComponent(state: VideoPlayerState): Component {
    NativeDiscovery().discover()
    val listener = object : MediaPlayerEventAdapter() {

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

    val component = if (isMacOS()) {
        CallbackMediaPlayerComponent()
    } else {
        EmbeddedMediaPlayerComponent()
    }

    component.mediaPlayer().events().addMediaPlayerEventListener(listener)

    return component
}

/**
 * We play the video again on finish (so the player is kind of idempotent),
 * unless the [onFinish] callback stops the playback.
 * Using `mediaPlayer.controls().repeat = true` did not work as expected.
 */
@Composable
private fun MediaPlayer.setupVideoFinishHandler(onFinish: (() -> Unit)?) {
    DisposableEffect(onFinish) {
        val listener = object : MediaPlayerEventAdapter() {
            override fun stopped(mediaPlayer: MediaPlayer) {
                onFinish?.invoke()
                mediaPlayer.controls().play()
            }
        }
        events().addMediaPlayerEventListener(listener)
        onDispose { events().removeMediaPlayerEventListener(listener) }
    }
}

/**
 * Returns [MediaPlayer] from player components.
 * The method names are the same, but they don't share the same parent/interface.
 * That's why we need this method.
 */
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
