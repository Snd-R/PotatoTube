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
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun VideoPlayerView(state: VideoPlayerState, modifier: Modifier) =
    Layout({
        var isHovered by remember { mutableStateOf(false) }
        var isClicked by remember { mutableStateOf(false) }
        val mrl by state.mrl.collectAsState()

        Box(
            modifier = Modifier
                .background(Color.Black)
                .fillMaxWidth()
                .then(modifier)
                .onPointerEvent(PointerEventType.Enter) { isHovered = true }
                .onPointerEvent(PointerEventType.Exit) { isHovered = false }
                .onPointerEvent(PointerEventType.Press) { isClicked = true }
                .onPointerEvent(PointerEventType.Release) { isClicked = false },
            contentAlignment = Alignment.Center
        ) {
            if (mrl != null)
                VlcEmbeddedVideoPlayer(state, modifier)
            else Box(Modifier.aspectRatio(1.735f))
        }
        VideoControlsOverlay(state, Modifier.fillMaxWidth())
    },
        measurePolicy = { measurables, constraints ->
            if (measurables.isEmpty()) return@Layout layout(0, 0) {}
            require(measurables.size == 2)

            val controls = measurables[1].measure(constraints.copy())
            val playerMeasurable = measurables[0]
            val playerHeight = playerMeasurable.maxIntrinsicHeight(constraints.maxWidth)
            val playerRealHeight = (playerHeight - controls.height).coerceAtLeast(0)
            val player = playerMeasurable.measure(
                constraints.copy(
                    maxHeight = playerRealHeight,
                    maxWidth = constraints.maxWidth
                )
            )

            layout(constraints.maxWidth, controls.height + player.height) {
                player.place(0, 0)
                controls.place(0, player.height)
            }
        })

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
        LinearProgressIndicator(
            progress = time / length.toFloat(),
            modifier = Modifier.fillMaxWidth(),
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
