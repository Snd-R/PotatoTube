package ui.videoplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.dp
import ui.common.LocalWindowHeight

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun VideoPlayerView(state: VideoPlayerState) {
    var isHovered by remember { mutableStateOf(false) }
    var isClicked by remember { mutableStateOf(false) }
    val isPlaying by state.isPlaying.collectAsState()
    val isBuffering by state.isBuffering.collectAsState()
    val mrl by state.mrl.collectAsState()
    val height = LocalWindowHeight.current
    Box(
        modifier = Modifier
            .background(Color.Black)
            .fillMaxSize()
            .heightIn(max = height - 100.dp)
            .fillMaxWidth()
            .onPointerEvent(PointerEventType.Enter) { isHovered = true }
            .onPointerEvent(PointerEventType.Exit) { isHovered = false }
            .onPointerEvent(PointerEventType.Press) { isClicked = true }
            .onPointerEvent(PointerEventType.Release) { isClicked = false },
    ) {
        if (mrl != null) {
            VideoPlayer(state, Modifier.align(Alignment.Center))
            if (!isPlaying || isHovered || isClicked || isBuffering) {
                VideoOverlay(state)
            }
        }
    }
}

@Composable
fun VideoOverlay(state: VideoPlayerState) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxHeight(0.4f)
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.2f),
                        )
                    )
                )
        )

        val isBuffering by state.isBuffering.collectAsState()
        if (isBuffering) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }

        VideoControlsOverlay(
            state, Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun VideoControlsOverlay(state: VideoPlayerState, modifier: Modifier = Modifier) {
    val time by state.timeState.time.collectAsState()
    val length by state.length.collectAsState()
    val isPlaying by state.isPlaying.collectAsState()

    Column(modifier = modifier) {
        Slider(
            value = time / length.toFloat(),
            onValueChange = { state.seekTo((it * length).toLong()) },
            modifier = Modifier.fillMaxWidth().height(15.dp),
            colors = SliderDefaults.colors(inactiveTrackColor = Color.LightGray),
            enabled = false,
        )
        Row(
            modifier = Modifier
                .padding(bottom = 10.dp, start = 20.dp)
                .align(Alignment.Start),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = "Pause",
                tint = Color.White,
                modifier = Modifier
                    .size(30.dp)
                    .clickable {
                        if (isPlaying) {
                            state.pause()
                        } else {
                            state.play()
                        }
                    }
            )
            VolumeControl(state)

            Text(
                "${state.currentTimeString()} / ${state.lengthString()}",
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }
}

@Composable
fun VolumeControl(state: VideoPlayerState) {
    val volumeInteractionSource = remember { MutableInteractionSource() }
    val isMuted by state.isMuted.collectAsState()
    val volume by state.volume.collectAsState()

    val volumeIcon =
        if (isMuted) Icons.Default.VolumeOff
        else if (volume in 1..50) Icons.Default.VolumeDown
        else if (volume > 50) Icons.Default.VolumeUp
        else Icons.Default.VolumeOff

    Row(modifier = Modifier.hoverable(volumeInteractionSource)) {
        Icon(
            volumeIcon,
            contentDescription = "Pause",
            tint = Color.White,
            modifier = Modifier
                .width(30.dp)
                .height(30.dp)
                .clickable {
                    state.toggleMute()
                }
        )
        val volumeState by remember { derivedStateOf { if (isMuted) 0 else volume } }

        Slider(
            value = volumeState.toFloat(),
            valueRange = 0f..100f,
            onValueChange = {
                if (isMuted) state.unmute()
                state.setVolume(it.toInt())
            },
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .height(15.dp)
                .width(90.dp),
        )
    }
}
