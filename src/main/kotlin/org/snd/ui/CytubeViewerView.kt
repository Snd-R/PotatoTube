package org.snd.ui

import androidx.compose.animation.core.Spring.StiffnessLow
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import org.snd.ui.chat.Chat
import org.snd.ui.chat.ChatView
import org.snd.ui.settings.SettingsView
import org.snd.ui.util.SplitterState


@Composable
fun CytubeView(model: Channel) {
    when (model.chat.currentScreen) {
        Chat.CurrentScreen.MAIN -> CytubeMainView(model)
        Chat.CurrentScreen.SETTINGS -> SettingsView(model.settings, model.chat)
    }
}

@Composable
fun CytubeMainView(model: Channel) {
    ChatView(model.chat, model.settings)

//    val focusManager = LocalFocusManager.current
//    val panelState = remember { PanelState() }
//    val animatedSize = if (panelState.splitter.isResizing) {
//        if (panelState.isExpanded) panelState.expandedSize else panelState.collapsedSize
//    } else {
//        animateDpAsState(
//            if (panelState.isExpanded) panelState.expandedSize else panelState.collapsedSize,
//            SpringSpec(stiffness = StiffnessLow)
//        ).value
//    }
//
//    VerticalSplittable(
//        Modifier
//            .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
//            .fillMaxSize(),
//        panelState.splitter,
//        onResize = {
//            panelState.expandedSize =
//                (panelState.expandedSize + it).coerceAtLeast(panelState.expandedSizeMin)
//        }
//    ) {
//        ResizablePanel(Modifier.width(animatedSize).fillMaxHeight(), panelState) {
//            ChatView(model.chat, model.settings)
//        }
//        Box(
//            modifier = Modifier
//        ) {
//            if (model.player.mrl != null)
//                VideoPlayerView(model.player)
//        }
//    }
}

private class PanelState {
    val collapsedSize = 24.dp
    var expandedSize by mutableStateOf(400.dp)
    val expandedSizeMin = 400.dp
    var isExpanded by mutableStateOf(true)
    val splitter = SplitterState()
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