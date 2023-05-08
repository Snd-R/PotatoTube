package ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ui.common.AppTheme
import ui.platform.Tooltip

@Composable
fun ChannelBar(model: ChatState) {
    val userCountState by model.users.userCount.collectAsState()
    Row {
        Column(
            modifier = Modifier
                .padding(horizontal = 5.dp, vertical = 5.dp),
        ) {
            val currentChannel = model.connectionStatus.currentChannel
            Box(
                modifier = Modifier
                    .clickable { }
                    .background(AppTheme.colors.backgroundDark)
            ) {
                val channelLabel = currentChannel ?: "Not connected"
                Text(channelLabel)
            }

            Row(Modifier.clickable { model.toggleUserList() }) {
                Icon(
                    Icons.Default.Group,
                    contentDescription = "Show Users",
                    tint = Color.LightGray,
                    modifier = Modifier.size(20.dp)
                )
                Tooltip(tooltip = { ConnectedUsersTooltip(model.users) }, delayMillis = 0) {
                    if (currentChannel != null) {
                        val users = if (userCountState == 1) "user" else "users"
                        Text(
                            "$userCountState connected $users",
                            fontSize = 13.sp,
                        )
                    }
                }
            }
        }

        val connectionErrorReason = model.connectionStatus.disconnectReason
        if (connectionErrorReason != null) {
            Text(
                text = "$connectionErrorReason",
                style = TextStyle(
                    color = MaterialTheme.colors.error,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }
}

@Composable
fun ConnectedUsersTooltip(model: ChatState.Users) {
    val siteAdmins by model.siteAdmins.collectAsState()
    val channelAdmins by model.channelAdmins.collectAsState()
    val moderators by model.moderators.collectAsState()
    val regularUsers by model.regularUsers.collectAsState()
    val guests by model.guests.collectAsState()
    val anonymous by model.anonymous.collectAsState()
    val afk by model.afk.collectAsState()

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(5))
            .border(width = 1.dp, color = Color.White, shape = RoundedCornerShape(5))
            .background(AppTheme.colors.backgroundDark)
            .padding(5.dp)
    ) {
        Column {
            Text("Site Admins: $siteAdmins", fontSize = 13.sp)
            Text("Channel Admins: $channelAdmins", fontSize = 13.sp)
            Text("Moderators: $moderators", fontSize = 13.sp)
            Text("Regular Users: $regularUsers", fontSize = 13.sp)
            Text("Guests: $guests", fontSize = 13.sp)
            Text("Anonymous: $anonymous", fontSize = 13.sp)
            Text("AFK: $afk", fontSize = 13.sp)
        }
    }

}