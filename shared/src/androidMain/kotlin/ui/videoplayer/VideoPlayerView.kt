package ui.videoplayer

import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


private val youtubeIdRegex = "(youtu.*be.*)/(watch\\?v=|embed/|v|shorts|)(.*?((?=[&#?])|\$))".toRegex()

@Composable
actual fun VideoPlayerView(state: VideoPlayerState, modifier: Modifier) {
    val mrlState by state.mrl.collectAsState()
    val mrl = mrlState

    if (mrl != null) {
        val youtubeId = youtubeIdRegex.find(mrl)?.groups?.get(3)?.value
        if (youtubeId != null) {
            YoutubePlayer(youtubeId, state)
        } else {
            ExoPlayer(mrl, state)
        }
    }
}

@Composable
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
fun ExoPlayer(uri: String, state: VideoPlayerState) {
    val context = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .build()
            .apply {
                val defaultDataSourceFactory = DefaultDataSource.Factory(context)
                val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(
                    context,
                    defaultDataSourceFactory
                )
                val source = ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(uri))

                setMediaSource(source)
                prepare()
            }
    }

    exoPlayer.playWhenReady = true
    exoPlayer.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
    exoPlayer.repeatMode = Player.REPEAT_MODE_OFF

    exoPlayer.addListener(object : Player.Listener {

    })

    val mrl by state.mrl.collectAsState()
    val updatedExternally by state.timeState.updatedExternallyToggle.collectAsState()
    val time by state.timeState.time.collectAsState()
    val isPlaying by state.isPlaying.collectAsState()

    LaunchedEffect(mrl) {
        launch {
            exoPlayer.setMediaItems(listOf(MediaItem.fromUri(uri)))
            exoPlayer.play()
        }
    }

    LaunchedEffect(updatedExternally) {
        launch {
            exoPlayer.seekTo(time)
        }
    }

    LaunchedEffect(isPlaying) {
        launch {
            if (isPlaying) {
                exoPlayer.play()
            } else {
                exoPlayer.pause()
            }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            state.timeState.updateInternally(exoPlayer.currentPosition)
            delay(50)
        }
    }

    DisposableEffect(
        AndroidView(factory = {
            PlayerView(context).apply {
                hideController()
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM

                player = exoPlayer
                layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            }
        })
    ) {
        onDispose { exoPlayer.release() }
    }
}

@Composable
fun YoutubePlayer(videoId: String, model: VideoPlayerState) {
    val context = LocalContext.current

    val updatedExternally by model.timeState.updatedExternallyToggle.collectAsState()
    val time by model.timeState.time.collectAsState()
    val isPlaying by model.isPlaying.collectAsState()
    var youtubePlayerState by remember { mutableStateOf<YouTubePlayer?>(null) }

    val youtubeView = remember {
        YouTubePlayerView(context).apply {
            enableAutomaticInitialization = false
            initialize(
                object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        youTubePlayer.loadVideo(videoId, 0f)
                        youtubePlayerState = youTubePlayer

                    }

                    override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                        if (state == PlayerConstants.PlayerState.ENDED) {
                            model.setMrl(null)
                        }
                    }

                    override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                        model.timeState.updateInternally((second * 1000).toLong())
                    }
                },
                IFramePlayerOptions.Builder()
                    .controls(1)
                    .rel(0)
                    .build()
            )
        }
    }

    LaunchedEffect(isPlaying) {
        val player = youtubePlayerState
        if (player != null) {
            launch {
                if (isPlaying) {
                    player.play()
                } else {
                    player.pause()
                }
            }
        }
    }

    LaunchedEffect(updatedExternally) {
        val player = youtubePlayerState
        if (player != null) {
            launch { player.seekTo(time / 1000.0f) }
        }
    }

    DisposableEffect(
        AndroidView(factory = { youtubeView })
    ) {
        onDispose { youtubeView.release() }
    }
}