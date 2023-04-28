package ui

import androidx.compose.animation.core.Spring.StiffnessLow
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import platform.SplitterState
import platform.VerticalSplittable
import ui.chat.ChatView
import ui.common.*
import ui.common.OverlayDialogValue.Hidden
import ui.playlist.PlaylistView
import ui.poll.PollMainView
import ui.settings.SettingsView
import ui.videoplayer.VideoPlayerView

@Composable
fun CytubeMainView(model: Channel) {
    val settingsOverlayState = remember { OverlayDialogState(Hidden) }
    OverlayDialog(
        overlayState = settingsOverlayState,
        overlayContent = { SettingsView(model.settings) }
    ) {

        when (LocalOrientation.current) {
            Orientation.LANDSCAPE -> HorizontalView(model)
            Orientation.PORTRAIT -> VerticalView(model)
        }
    }

    if (model.chat.settings.isActiveScreen) {
        settingsOverlayState.open()
    } else {
        settingsOverlayState.close()
    }
}

@Composable
private fun VerticalView(model: Channel) {

    Column {
        Box(modifier = Modifier.aspectRatio(1.735f)) {
            VideoPanel(model)
        }
        Box(modifier = Modifier.fillMaxSize()) {

            ChatView(model.chat, model.settings)
        }
    }
}


@Composable
private fun HorizontalView(model: Channel) {
    val focusManager = LocalFocusManager.current
    val windowSize = LocalWindowSize.current
    val panelState = remember { PanelState() }
    panelState.setSize(windowSize)
    val animatedSize = if (panelState.splitter.isResizing) {
        if (panelState.isExpanded) panelState.expandedSize else panelState.collapsedSize
    } else {
        animateDpAsState(
            if (panelState.isExpanded) panelState.expandedSize else panelState.collapsedSize,
            SpringSpec(stiffness = StiffnessLow)
        ).value
    }
    VerticalSplittable(
        Modifier
            .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
            .fillMaxSize(),
        panelState.splitter,
        onResize = {
            panelState.expandedSize =
                (panelState.expandedSize + it).coerceAtLeast(panelState.expandedSizeMin)
        }
    ) {
        ResizablePanel(Modifier.width(animatedSize).fillMaxHeight(), panelState) {
            ChatView(model.chat, model.settings)
        }
        VideoPanel(model)
    }

}

@Composable
private fun ResizablePanel(
    modifier: Modifier,
    state: PanelState,
    content: @Composable () -> Unit,
) {
    val alpha by animateFloatAsState(if (state.isExpanded) 1f else 0f, SpringSpec(stiffness = StiffnessLow))

    Box(modifier) {
        Box(Modifier.fillMaxSize().graphicsLayer(alpha = alpha)) {
            content()
        }

        Icon(
            if (state.isExpanded) Icons.Default.ArrowBack else Icons.Default.ArrowForward,
            contentDescription = if (state.isExpanded) "Collapse" else "Expand",
            tint = LocalContentColor.current,
            modifier = Modifier
                .padding(top = 4.dp)
                .width(24.dp)
                .clickable {
                    state.isExpanded = !state.isExpanded
                }
                .padding(4.dp)
                .align(Alignment.TopEnd)
        )
    }
}

@Composable
private fun VideoPanel(model: Channel) {

    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier.verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VideoPlayerView(model.player)
        Spacer(Modifier.size(5.dp))
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .padding(start = 10.dp, top = 10.dp)
                    .fillMaxWidth(0.5f)
            ) {
                if (model.poll.currentPoll)
                    PollMainView(model.poll)
            }
            Box(
                modifier = Modifier
                    .heightIn(max = 400.dp)
                    .fillMaxWidth()
            ) {
                PlaylistView(model.playlist)
            }
        }
    }
}

private class PanelState {
    var collapsedSize: Dp by mutableStateOf(24.dp)
    var expandedSizeMin: Dp by mutableStateOf(300.dp)
    var expandedSize by mutableStateOf(400.dp)
    var isExpanded by mutableStateOf(true)
    val splitter = SplitterState()

    fun setSize(windowSize: WindowSize) {
        when (windowSize) {
            WindowSize.FULL -> {
                collapsedSize = 24.dp
                expandedSizeMin = 350.dp
                expandedSize =
                    if (this.expandedSize.value > 1000) 1000.dp else if (this.expandedSize < expandedSizeMin) this.expandedSizeMin else this.expandedSize

            }

            else -> {
                collapsedSize = 24.dp
                expandedSizeMin = 200.dp
                expandedSize =
                    if (this.expandedSize.value > 250 || this.expandedSize < expandedSizeMin) 250.dp else this.expandedSize

            }
        }
    }
}