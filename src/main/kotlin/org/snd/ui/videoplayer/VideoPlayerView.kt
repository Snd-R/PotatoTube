package org.snd.ui.videoplayer


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
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
fun VideoPlayerView(state: VideoPlayerState) {
    Box(modifier = Modifier.aspectRatio(1.77f)) {
        VideoPlayer(state, Modifier.fillMaxSize())

        val time by state.time.collectAsState()
        val isPlaying by state.isPlaying.collectAsState()
        val length by state.length.collectAsState()

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
        ) {
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { state.seekTo(state.time.value.time - 5000) }
                ) {
                    Text("Backward")
                }
                Button(
                    onClick = {
                        if (isPlaying) {
                            state.pause()
                        } else {
                            state.play()
                        }
                    }
                ) {
                    Text(if (isPlaying) "Pause" else "Play")
                }
                Button(
                    onClick = {
                        state.seekTo(time.time + 5000)
                    }
                ) {
                    Text("Forward")
                }
            }

            Slider(
                value = time.time / length.length.toFloat(),
                onValueChange = { state.seekTo((it * length.length).toLong()) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

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
        Box(modifier = modifier.background(Color.Gray))
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
        embeddedMediaPlayer
    }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(key1 = state.mrl) {
        mediaPlayer.media().play(state.mrl)
        mediaPlayer.events().addMediaPlayerEventListener(object : MediaPlayerEventAdapter() {

            override fun mediaPlayerReady(mediaPlayer: MediaPlayer) {
                super.mediaPlayerReady(mediaPlayer)
                state.lengthMutable.value = VideoLength(mediaPlayer.status().length())
                if (!state.isPlaying.value) {
                    mediaPlayer.controls().pause()
                }
            }

            override fun finished(mediaPlayer: MediaPlayer) {
                super.finished(mediaPlayer)
                state.isPlaying.value = false
            }

            override fun timeChanged(mediaPlayer: MediaPlayer, newTime: Long) {
                super.timeChanged(mediaPlayer, newTime)
                state.time.value = TimeState.internalTime(newTime)
            }
        })

        coroutineScope.launch {
            state.isPlaying.collect {
                if (it) {
                    mediaPlayer.controls().start()
                } else {
                    mediaPlayer.controls().pause()
                }
            }
        }

        var firstUpdate = true
        coroutineScope.launch {
            state.time.collect {
                if (firstUpdate || !it.updatedInternally) {
                    mediaPlayer.controls().setTime(it.time)
                }
                firstUpdate = false
            }
        }
    }

    DisposableEffect(key1 = state.mrl, effect = {
        this.onDispose {
            mediaPlayer.release()
        }
    })
}


