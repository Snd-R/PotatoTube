package org.snd.ui.videoplayer


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun VideoPlayerView(state: VideoPlayerState) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    Box(
        modifier = Modifier
            .background(Color.Black)
            .aspectRatio(1.735f)
            .hoverable(interactionSource = interactionSource)
    ) {
        VideoPlayer(state, Modifier.matchParentSize())
        if (!state.isPlaying || isHovered || state.isBuffering) {

            Box {
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

        if (state.isBuffering) {
            println("buffering")
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }

        VideoControlsOverlay(
            state, Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun VideoControlsOverlay(state: VideoPlayerState, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Slider(
            value = state.time.value / state.length.toFloat(),
            onValueChange = { state.seekTo((it * state.length).toLong()) },
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
                if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = "Pause",
                tint = Color.White,
                modifier = Modifier
                    .size(30.dp)
                    .clickable {
                        if (state.isPlaying) {
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

    val volumeIcon =
        if (state.isMuted) Icons.Default.VolumeOff
        else if (state.volume in 1..50) Icons.Default.VolumeDown
        else if (state.volume > 50) Icons.Default.VolumeUp
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
        val volumeState by remember { derivedStateOf { if (state.isMuted) 0 else state.volume } }

        Slider(
            value = volumeState.toFloat(),
            valueRange = 0f..100f,
            onValueChange = {
                if (state.isMuted) state.toggleMute()
                state.volume = it.toInt()
            },
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .height(15.dp)
                .width(90.dp),
        )
    }
}
