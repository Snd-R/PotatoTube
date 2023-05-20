package ui.videoplayer

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.dp
import player.Player
import player.PlayerType
import ui.common.LocalOrientation
import ui.common.LocalWindowHeight
import ui.common.Orientation.LANDSCAPE

@Composable
actual fun VideoPlayerView(
    state: VideoPlayerState,
    type: PlayerType,
    modifier: Modifier,
    scrollState: ScrollState
) =
    Layout({
        val heightModifier = if (LocalOrientation.current == LANDSCAPE && state.isInTheaterMode) {
            Modifier.height(LocalWindowHeight.current)
        } else Modifier

        Box(
            modifier = Modifier
                .background(Color.Black)
                .fillMaxWidth()
                .then(modifier)
                .then(heightModifier),
            contentAlignment = Alignment.Center
        ) {
            val mrl by state.mrl.collectAsState()
            if (mrl != null) when (type.player) {
                Player.VLC_DIRECT -> VlcDirectRenderVideoPlayer(state, modifier)
                Player.VLC_EMBEDDED -> VlcEmbeddedVideoPlayer(state, modifier, scrollState)
                Player.MPV_EMBEDDED -> MpvEmbeddedVideoPlayer(state, modifier, scrollState)
            }
            else Box(Modifier.aspectRatio(1.735f))
        }
        VideoControls(state, Modifier.background(Color.Black).fillMaxWidth())
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
fun VideoControls(state: VideoPlayerState, modifier: Modifier = Modifier) {
    val time by state.timeState.time.collectAsState()
    val length by state.length.collectAsState()
    val isPlaying by state.isPlaying.collectAsState()
    val orientation = LocalOrientation.current

    Column(modifier = modifier) {
        LinearProgressIndicator(
            progress = time / length.toFloat(),
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier
                .padding(bottom = 10.dp, start = 20.dp, end = 20.dp)
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
            if (orientation == LANDSCAPE) {
                Spacer(Modifier.weight(1.0f))
                Icon(
                    Icons.Default.FitScreen,
                    contentDescription = "Theatre Mode",
                    tint = Color.White,
                    modifier = Modifier
                        .size(30.dp)
                        .clickable { state.toggleTheaterMode() }
                )
            }
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
