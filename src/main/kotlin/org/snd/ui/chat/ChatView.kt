package org.snd.ui.chat

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerEventType.Companion.Scroll
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.snd.ui.common.AppTheme
import org.snd.ui.poll.PollChatView
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
                MessageInputView(chat = model)
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
                            .size(25.dp)
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
                            .size(25.dp)
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
            LazyColumn(state = scrollState) {
                items(model.messages.size) {
                    SelectionContainer {
                        ChatMessage(
                            settings.fontSize,
                            settings.emoteSize,
                            20.sp.toDp() * 1.5f,
                            it,
                            model.messages[it],
                            model,
                        )
                    }
                }
            }

            VerticalScrollbar(
                Modifier.align(Alignment.CenterEnd),
                scrollState
            )

            if (model.poll.currentPoll && model.poll.showChatPoll) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 15.dp)
                        .padding(top = 5.dp, start = 3.dp, end = 3.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, color = AppTheme.colors.backgroundLight, shape = RoundedCornerShape(10.dp))
                        .background(color = AppTheme.colors.backgroundDark)
                ) {
                    PollChatView(model.poll)
                }
            }

            val endOfListReached by remember {
                derivedStateOf { scrollState.isScrolledToEnd() || model.messages.isEmpty() }
            }
            if (endOfListReached) model.scrolledUp = false

            LaunchedEffect(model.messages.size, endOfListReached) {
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
fun ActiveUser(user: Chat.User) {
    val (color, weight) = when (user.rank) {
        Chat.UserRank.GUEST -> AppTheme.colors.guest to FontWeight.Normal
        Chat.UserRank.REGULAR_USER -> AppTheme.colors.regularUser to FontWeight.Normal
        Chat.UserRank.MODERATOR -> AppTheme.colors.moderator to FontWeight.Normal
        Chat.UserRank.CHANNEL_ADMIN -> AppTheme.colors.channelAdmin to FontWeight.Bold
        Chat.UserRank.SITE_ADMIN -> AppTheme.colors.siteAdmin to FontWeight.Bold
    }
    Row {
        if (user.afk) {
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
            user.name,
            fontSize = 13.sp,
            color = color,
            fontWeight = weight,
            modifier = Modifier.align(Alignment.Top)
        )
    }
}

fun LazyListState.isScrolledToEnd() = layoutInfo.visibleItemsInfo.lastOrNull()?.index == layoutInfo.totalItemsCount - 1
