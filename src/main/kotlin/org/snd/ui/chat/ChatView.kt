package org.snd.ui.chat

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerEventType.Companion.Scroll
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.snd.ui.common.AppTheme
import org.snd.ui.settings.SettingsModel
import org.snd.ui.settings.SettingsView
import org.snd.ui.util.VerticalScrollbar
import java.util.*


@Composable
fun ChatView(model: Chat, settings: SettingsModel) = Surface(
    modifier = Modifier.fillMaxSize()
) {
    if (model.settings.isActiveScreen) {
        Box(
            Modifier
                .fillMaxSize()
        ) {
            SettingsView(model.settings)
        }
    } else {
        println("recomposing chat view")
        Column {
            ChannelBar(model)
            Divider()
            Row(modifier = Modifier.weight(1f)) {
                UserList(model)
                Divider(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                )
                MessageBox(model, settings)
            }
            Column(modifier = Modifier.background(AppTheme.colors.backgroundDarker)) {
                MessageInputView(
                    chat = model,
                ) {
                    model.sendMessage(it)
                }
                var emoteMenu by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(vertical = 5.dp, horizontal = 3.dp)
                        .heightIn(max = 400.dp)
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.LightGray,
                        modifier = Modifier
                            .width(30.dp)
                            .height(30.dp)
                            .clickable {
                                model.settings.isActiveScreen = true
                            }
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    Icon(
                        Icons.Default.Mood,
                        contentDescription = "Emotes",
                        tint = if (emoteMenu) AppTheme.colors.buttonActive else Color.LightGray,
                        modifier = Modifier
                            .width(30.dp)
                            .height(30.dp)
                            .clickable(enabled = model.channelEmotes.isNotEmpty()) {
                                emoteMenu = !emoteMenu
                            }
                    )

                    if (emoteMenu) {
                        Box(
                            modifier = Modifier
                                .heightIn(min = 0.dp, max = 400.dp)
                                .widthIn(min = 0.dp, max = 600.dp)
                        ) {
                            EmoteMenuView(model)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun MessageBox(model: Chat, settings: SettingsModel) {
    println("recomposing message box")
    with(LocalDensity.current) {
        Box(
            modifier = Modifier.fillMaxSize()
                .background(AppTheme.colors.backgroundDark)
                .onPointerEvent(eventType = Scroll) { event ->
                    if (event.changes.any { it.scrollDelta.y <= -1.0 })
                        model.scrolledUp = true
                }
        ) {
            val scrollState = rememberLazyListState()
            LazyColumn(state = scrollState) { //TODO use regular column to cache images and avoid recompositions on new message?
                items(model.messages.size) {
                    SelectionContainer {
                        ChatMessage(
                            settings.fontSize,
                            settings.emoteSize,
                            20.sp.toDp() * 1.5f,
                            it,
                            model.messages[it],
                            model,
                            settings.timestampFormat
                        )
                    }
                }
            }

            VerticalScrollbar(
                Modifier.align(Alignment.CenterEnd),
                scrollState
            )

            val endOfListReached by remember {
                derivedStateOf { scrollState.isScrolledToEnd() || model.messages.isEmpty() }
            }
            if (endOfListReached) model.scrolledUp = false

            LaunchedEffect(model.messages.size) {
                if (model.messages.isNotEmpty() && !model.scrolledUp)
                    scrollState.animateScrollToItem(model.messages.size - 1)
            }
        }
    }
}


@Composable
fun UserList(model: Chat) {
    Box(
        modifier = Modifier
            .widthIn(min = 60.dp)
            .background(AppTheme.colors.backgroundDarker)
    ) {
        val scrollState = rememberLazyListState()
        LazyColumn(state = scrollState) {
            items(model.users.users.size) {
                ActiveUser(model.users.users[it])
            }
        }

        VerticalScrollbar(
            Modifier.align(Alignment.CenterEnd),
            scrollState
        )
    }
}

@Composable
fun ActiveUser(model: Chat.User) {
    Row {
        if (model.afk) {
            Icon(
                Icons.Default.Schedule,
                contentDescription = "AFK",
                tint = LocalContentColor.current,
                modifier = Modifier
                    .width(14.dp)
                    .align(Alignment.CenterVertically)
            )
        }
        Text(
            model.name,
            fontSize = 13.sp,
            modifier = Modifier.align(Alignment.Top)
        )
    }
}

fun LazyListState.isScrolledToEnd() = layoutInfo.visibleItemsInfo.lastOrNull()?.index == layoutInfo.totalItemsCount - 1
