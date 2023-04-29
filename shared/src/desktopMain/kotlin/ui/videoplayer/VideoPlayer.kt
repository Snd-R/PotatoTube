package ui.videoplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
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
    modifier: Modifier,
) {
    var bitmap by remember { mutableStateOf(Bitmap()) }
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    imageBitmap?.let {
        androidx.compose.foundation.Image(
            bitmap = it,
            contentDescription = "Video",
            modifier = modifier.then(Modifier.fillMaxSize())
        )
    } ?: run {
        Box(modifier = modifier.background(Color.Black))
    }

    val mediaPlayer = remember {
        var byteArray: ByteArray? = null
        val factory = MediaPlayerFactory()
        val embeddedMediaPlayer = factory.mediaPlayers().newEmbeddedMediaPlayer()
        val callbackVideoSurface = CallbackVideoSurface(
            object : BufferFormatCallback {
                override fun getBufferFormat(sourceWidth: Int, sourceHeight: Int): BufferFormat {
                    val imageInfo = ImageInfo.makeN32(sourceWidth, sourceHeight, ColorAlphaType.OPAQUE)
                    bitmap = Bitmap().apply { allocPixels(imageInfo) }
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

                    bitmap.installPixels(byteArray)
                    imageBitmap = bitmap.asComposeImageBitmap()
                }
            },
            true,
            VideoSurfaceAdapters.getVideoSurfaceAdapter(),
        )
        embeddedMediaPlayer.videoSurface().set(callbackVideoSurface)

        embeddedMediaPlayer.events().addMediaPlayerEventListener(object : MediaPlayerEventAdapter() {

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
        })
        embeddedMediaPlayer
    }

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

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.release()
        }
    }
}
