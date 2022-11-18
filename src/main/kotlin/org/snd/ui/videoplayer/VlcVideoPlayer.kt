package org.snd.ui.videoplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ImageInfo
import uk.co.caprica.vlcj.factory.MediaPlayerFactory
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurfaceAdapters
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat
import java.nio.ByteBuffer

@Composable
fun VideoPlayer(
    state: VideoPlayerState,
    modifier: Modifier = Modifier,
) {
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    imageBitmap?.let {
        androidx.compose.foundation.Image(
            bitmap = it,
            contentDescription = "Video",
            modifier = modifier
        )
    } ?: run {
        Box(modifier = modifier.background(Color.Black))
    }

    val mediaPlayer = remember {
        var byteArray: ByteArray? = null
        var info: ImageInfo? = null
        val factory = MediaPlayerFactory()
        val embeddedMediaPlayer = factory.mediaPlayers().newEmbeddedMediaPlayer()
        val callbackVideoSurface = CallbackVideoSurface(
            object : BufferFormatCallback {
                override fun getBufferFormat(sourceWidth: Int, sourceHeight: Int): BufferFormat {
                    info = ImageInfo.makeN32(sourceWidth, sourceHeight, ColorAlphaType.OPAQUE)
                    return RV32BufferFormat(sourceWidth, sourceHeight)
                }

                override fun allocatedBuffers(buffers: Array<out ByteBuffer>) {
                    byteArray = ByteArray(buffers[0].limit())
                }
            },
            object : RenderCallback {
                override fun display(
                    mediaPlayer: MediaPlayer,
                    nativeBuffers: Array<out ByteBuffer>,
                    bufferFormat: BufferFormat?
                ) {
                    val byteBuffer = nativeBuffers[0]

                    byteBuffer.get(byteArray)
                    byteBuffer.rewind()

                    val bmp = Bitmap()
                    bmp.allocPixels(info!!)
                    bmp.installPixels(byteArray)
                    imageBitmap = bmp.asComposeImageBitmap()
                }
            },
            true,
            VideoSurfaceAdapters.getVideoSurfaceAdapter(),
        )
        embeddedMediaPlayer.videoSurface().set(callbackVideoSurface)

        embeddedMediaPlayer.events().addMediaPlayerEventListener(object : MediaPlayerEventAdapter() {

            override fun mediaPlayerReady(mediaPlayer: MediaPlayer) {
                super.mediaPlayerReady(mediaPlayer)
                state.length = mediaPlayer.status().length()
                state.isPlaying = true
            }

            override fun finished(mediaPlayer: MediaPlayer) {
                super.finished(mediaPlayer)
                state.isPlaying = false
                state.mrl = null
                state.time.updateInternally(0L)
            }

            override fun timeChanged(mediaPlayer: MediaPlayer, newTime: Long) {
                super.timeChanged(mediaPlayer, newTime)
                state.time.updateInternally(newTime)
            }

            override fun buffering(mediaPlayer: MediaPlayer?, newCache: Float) {
                state.isBuffering = newCache != 100.0f
            }
        })
        embeddedMediaPlayer
    }

    if (state.mrl != null) {
        LaunchedEffect(key1 = state.mrl) {
            val mrl = state.mrl
            withContext(Dispatchers.IO) {
                launch { mediaPlayer.media().play(mrl) }
            }
        }

        LaunchedEffect(state.time.updatedExternallyToggle) {
            val time = state.time.value
            withContext(Dispatchers.IO) {
                launch { mediaPlayer.controls().setTime(time) }
            }
        }

        LaunchedEffect(state.isPlaying) {
            val isPlaying = state.isPlaying
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
        LaunchedEffect(state.volume) {
            val volume = state.volume
            withContext(Dispatchers.IO) {
                launch { mediaPlayer.audio().setVolume(volume) }
            }
        }

        LaunchedEffect(state.isMuted) {
            withContext(Dispatchers.IO) {
                launch { mediaPlayer.audio().mute() }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.release()
        }
    }

}
