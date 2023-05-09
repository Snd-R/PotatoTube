package ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ui.common.AppTheme
import ui.platform.VerticalScrollbar
import ui.poll.PollChatView
import ui.settings.SettingsState

@Composable
fun ChatView(model: ChatState) = Surface(
    modifier = Modifier.fillMaxSize()
) {
    ChatPanel(model)
}

@Composable
private fun ChatPanel(model: ChatState) {
    Column {
        ChannelBar(model)
        Divider()
        Row(modifier = Modifier.weight(1f)) {
            if (model.showUserList) {
                UserList(model)
                Divider(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                )
            }

            MessageBox(model, model.settings)
        }
        Column(modifier = Modifier.background(AppTheme.colors.backgroundDarker)) {
            MessageInputView(chat = model)
            var emoteMenu by remember { mutableStateOf(false) }
            val channelEmotesState by model.channelEmotes.collectAsState()

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
                        .clickable(enabled = channelEmotesState.isNotEmpty()) {
                            emoteMenu = !emoteMenu
                        }
                )


            }

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

@Composable
private fun MessageBox(model: ChatState, settings: SettingsState) {
    with(LocalDensity.current) {
        Box(
            modifier = Modifier.fillMaxSize()
                .background(AppTheme.colors.backgroundDark)
        ) {
            val scrolledUp by model.scrolledUp.collectAsState()

            val scrollState = rememberLazyListState()
            val viewportSize by remember { derivedStateOf { scrollState.layoutInfo.viewportSize } }
            val messagesState by model.messages.collectAsState()

            LazyColumn(
                state = scrollState,
                modifier = Modifier.onScroll {
                    model.setScrollState(true)
                },
            ) {
                items(messagesState.size) {
                    SelectionContainer {
                        ChatMessage(
                            settings.fontSize,
                            settings.emoteSize,
                            20.sp.toDp() * 1.5f,
                            it,
                            messagesState[it],
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
//            if (!scrollState.canScrollForward)
//                model.setScrollState(false)

            LaunchedEffect(messagesState.size, viewportSize, scrolledUp, scrollState.canScrollForward) {
                if (!scrollState.canScrollForward) model.setScrollState(false)
                if (messagesState.isNotEmpty() && !scrolledUp && !scrollState.isScrollInProgress) {
                    scrollState.animateScrollToItem(messagesState.size - 1)
                }
            }
        }
    }
}

@Composable
fun UserList(model: ChatState) {
    Box(
        modifier = Modifier
            .widthIn(min = 100.dp, max = 100.dp)
            .background(AppTheme.colors.backgroundDarker)
    ) {
        val scrollState = rememberLazyListState()
        val userListState by model.users.users.collectAsState()
        LazyColumn(state = scrollState) {
            items(userListState.size) {
                ActiveUser(userListState[it])
            }
        }

        VerticalScrollbar(
            Modifier.align(Alignment.CenterEnd),
            scrollState
        )
    }
}

@Composable
fun ActiveUser(user: ChatState.User) {
    val userRankState by user.rank.collectAsState()
    val userAfkState by user.afk.collectAsState()
    val (color, weight) = when (userRankState) {
        ChatState.UserRank.GUEST -> AppTheme.colors.guest to FontWeight.Normal
        ChatState.UserRank.REGULAR_USER -> AppTheme.colors.regularUser to FontWeight.Normal
        ChatState.UserRank.MODERATOR -> AppTheme.colors.moderator to FontWeight.Normal
        ChatState.UserRank.CHANNEL_ADMIN -> AppTheme.colors.channelAdmin to FontWeight.Bold
        ChatState.UserRank.SITE_ADMIN -> AppTheme.colors.siteAdmin to FontWeight.Bold
    }
    Row {
        if (userAfkState) {
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

expect fun Modifier.onScroll(onScroll: () -> Unit): Modifier