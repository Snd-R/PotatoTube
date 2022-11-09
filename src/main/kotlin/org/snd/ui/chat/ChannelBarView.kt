package org.snd.ui.chat

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.snd.ui.common.AppTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChannelBar(model: Chat) {
    Column(
        modifier = Modifier
            .padding(horizontal = 5.dp, vertical = 5.dp),
    ) {
        Box(
            modifier = Modifier
                .clickable { }
                .background(AppTheme.colors.backgroundDark)
        ) {
            val currentChannel =
                if (model.cytubeState.currentChannel != null) model.cytubeState.currentChannel!!
                else "Not connected"
            Text(currentChannel)
        }
        TooltipArea(tooltip = { ConnectedUsersTooltip(model.users) }) {
            val users = if (model.users.userCount == 1) "user" else "users"
            Text("${model.users.userCount} connected $users", fontSize = 13.sp)
        }
    }
}

@Composable
fun ConnectedUsersTooltip(model: Chat.Users) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(5))
            .border(width = 1.dp, color = Color.White, shape = RoundedCornerShape(5))
            .background(AppTheme.colors.backgroundDark)
            .padding(5.dp)
    ) {
        Column(
        ) {
            Text("Site Admins: ${model.siteAdmins}", fontSize = 13.sp)
            Text("Channel Admins: ${model.channelAdmins}", fontSize = 13.sp)
            Text("Moderators: ${model.moderators}", fontSize = 13.sp)
            Text("Regular Users: ${model.regularUsers}", fontSize = 13.sp)
            Text("Guests: ${model.guests}", fontSize = 13.sp)
            Text("Anonymous: ${model.anonymous}", fontSize = 13.sp)
            Text("AFK: ${model.afk}", fontSize = 13.sp)
        }
    }

}