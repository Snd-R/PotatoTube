package ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ui.common.AppTheme
import ui.common.LoadingOverlay
import ui.platform.VerticalScrollbar
import ui.poll.PollChatView
import ui.settings.SettingsState

@Composable
fun ChatView(state: ChatState) {
    Column(Modifier.fillMaxSize()) {
        ChannelBar(state)
        Divider()
        Row(modifier = Modifier.weight(1f)) {
            if (state.showUserList) {
                UserList(state)
                Divider(modifier = Modifier.width(1.dp).fillMaxHeight())
            }
            MessageBox(state, state.settings)
        }
        Column(Modifier.background(AppTheme.colors.backgroundDarker)) {
            MessageInputView(state)
            ExtensionButtons(state, Modifier.align(Alignment.Start))
        }
    }
    if (state.isLoading) LoadingOverlay()
}

@Composable
private fun MessageBox(model: ChatState, settings: SettingsState) {
    Box(
        modifier = Modifier.fillMaxSize()
            .background(AppTheme.colors.backgroundDark)
    ) {
        val scrolledUp by model.isScrolledUp.collectAsState()

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

        val currentPoll by model.poll.currentPoll.collectAsState()
        val showChatPoll by model.poll.showChatPoll.collectAsState()
        if (currentPoll && showChatPoll) {
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
        if (!scrollState.canScrollForward)
            model.setScrollState(false)

        LaunchedEffect(messagesState.size, viewportSize, scrolledUp, scrollState.canScrollForward) {
            if (messagesState.isNotEmpty() && !scrolledUp && !scrollState.isScrollInProgress && scrollState.canScrollForward) {
                scrollState.scrollToItem(messagesState.size - 1)
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

@Composable
private fun ExtensionButtons(state: ChatState, modifier: Modifier) {
    var showEmoteMenu by remember { mutableStateOf(false) }
    val channelEmotes by state.channelEmotes.collectAsState()
    Row(
        modifier = Modifier
            .padding(vertical = 5.dp, horizontal = 3.dp)
            .heightIn(max = 400.dp)
            .then(modifier)
    ) {
        Icon(
            Icons.Default.Settings,
            contentDescription = "Settings",
            tint = Color.LightGray,
            modifier = Modifier
                .size(25.dp)
                .clickable { state.showSettingsOverlay = true }
        )
        Spacer(modifier = Modifier.size(10.dp))
        Icon(
            Icons.Default.Mood,
            contentDescription = "Emotes",
            tint = if (showEmoteMenu) AppTheme.colors.buttonActive else Color.LightGray,
            modifier = Modifier
                .size(25.dp)
                .clickable(enabled = channelEmotes.isNotEmpty()) {
                    showEmoteMenu = !showEmoteMenu
                }
        )
    }

    if (showEmoteMenu) {
        Box(
            modifier = Modifier
                .heightIn(max = 400.dp)
                .widthIn(max = 600.dp)
        ) {
            EmoteMenuView(state)
        }
    }
}

expect fun Modifier.onScroll(onScroll: () -> Unit): Modifier