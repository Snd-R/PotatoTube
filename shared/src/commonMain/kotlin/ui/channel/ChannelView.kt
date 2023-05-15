package ui.channel

import androidx.compose.animation.core.Spring.StiffnessLow
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ui.chat.ChatView
import ui.common.*
import ui.common.OverlayDialogValue.Hidden
import ui.playlist.PlaylistView
import ui.poll.PollMainView
import ui.settings.SettingsView
import ui.videoplayer.VideoPlayerView

@Composable
fun ChannelView(model: ChannelState) {
    val settings = model.settings
    LaunchedEffect(settings.channel, settings.username) {
        model.init()
    }

    val settingsOverlayState = remember { OverlayDialogState(Hidden) }
    OverlayDialog(
        overlayState = settingsOverlayState,
        overlayContent = { SettingsView(model.settings, onDismiss = { model.chat.showSettingsOverlay = false }) }
    ) {
        when (LocalOrientation.current) {
            Orientation.LANDSCAPE -> HorizontalView(model)
            Orientation.PORTRAIT -> VerticalView(model)
        }
    }

    if (model.chat.showSettingsOverlay) settingsOverlayState.open()
    else settingsOverlayState.close()

    DisposableEffect(Unit) {
        onDispose { model.disconnect() }
    }
}

@Composable
private fun VerticalView(model: ChannelState) {
    Column {
        Box(modifier = Modifier.aspectRatio(1.735f)) {
            VideoPanel(model)
        }
        Box(modifier = Modifier.fillMaxSize()) {
            ChatView(model.chat)
        }
    }
}


@Composable
private fun HorizontalView(model: ChannelState) {
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
            ChatView(model.chat)
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
private fun VideoPanel(model: ChannelState) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier.verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VideoPlayer(model, scrollState)
        Spacer(Modifier.size(5.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(Modifier.weight(1.0f))

            if (model.poll.currentPoll) {
                Box(
                    modifier = Modifier
                        .padding(start = 10.dp, top = 10.dp)
                        .fillMaxWidth(0.5f)
                ) {
                    PollMainView(model.poll)
                }
            }

            Box(
                modifier = Modifier
                    .heightIn(max = 400.dp)
                    .widthIn(max = 1000.dp)
                    .fillMaxWidth()
            ) {
                PlaylistView(model.playlist)
            }
        }
    }
}

@Composable
private fun VideoPlayer(model: ChannelState, scrollState: ScrollState) {
    val coroutineScope = rememberCoroutineScope()
    val playerHeight = if (model.chat.showSettingsOverlay) Modifier.size(0.dp)
    else when (LocalOrientation.current) {
        Orientation.LANDSCAPE -> Modifier.heightIn(min = 0.dp, max = LocalWindowHeight.current)
        Orientation.PORTRAIT -> Modifier
    }

    Box {
        model.settings.playerType?.let { player ->
            VideoPlayerView(
                model.player,
                player,
                playerHeight,
                scrollState,
            )
        } ?: run {
            Box(
                modifier = Modifier.background(Color.Black)
                    .then(playerHeight)
                    .aspectRatio(1.735f),
                contentAlignment = Alignment.Center
            ) { Text("Player Unavailable") }

        }
        val windowSize = LocalWindowSize.current
        val maxScroll = with(LocalDensity.current) { scrollState.maxValue.toDp() }
        if (windowSize != WindowSize.FULL && windowSize != WindowSize.EXPANDED
            && maxScroll > 50.dp && scrollState.canScrollForward
        ) {
            TextButton(
                modifier =
                if (LocalOrientation.current == Orientation.PORTRAIT) Modifier.align(Alignment.BottomEnd)
                else Modifier.align(Alignment.BottomCenter),
                onClick = {
                    coroutineScope.launch {
                        scrollState.animateScrollTo(scrollState.maxValue)
                    }
                }) {
                Text("Scroll down")
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