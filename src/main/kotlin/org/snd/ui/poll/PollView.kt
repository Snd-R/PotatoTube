package org.snd.ui.poll

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.snd.ui.common.AppTheme

@Composable
fun PollChatView(poll: PollState) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(10.dp)) {
        Row {
            Column {
                Row(modifier = Modifier.clickable {
                    expanded = !expanded
                }
                ) {
                    if (poll.closed) Text("Poll ended", fontSize = 12.sp, modifier = Modifier.padding(top = 3.dp))
                    else
                        Text("Current Poll", fontSize = 12.sp, modifier = Modifier.padding(top = 3.dp))
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Options",
                        tint = Color.LightGray,
                        modifier = Modifier
                            .size(20.dp)
                    )
                }
                Text(poll.title, modifier = Modifier.padding(start = 10.dp))
            }


            Spacer(Modifier.weight(1f))
            Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.LightGray,
                modifier = Modifier
                    .width(20.dp)
                    .height(20.dp)
                    .clickable {
                        poll.hideChatPoll()
                    }
            )
        }

        if (expanded) {
            PollOptions(poll)
        }
    }
}

@Composable
fun PollMainView(poll: PollState) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(AppTheme.colors.backgroundDark)
            .widthIn(min = 300.dp, max = 300.dp)
//            .fillMaxWidth(0.7f)
            .padding(10.dp)
    ) {
        Row(modifier = Modifier.padding(start = 5.dp, end = 5.dp, bottom = 5.dp)) {
            Text(
                poll.title,
                modifier = Modifier.padding(start = 5.dp),
                maxLines = 2,
            )
            if (poll.closed) {
                Spacer(Modifier.weight(1f))
                Icon(
                    Icons.Default.Block,
                    contentDescription = "Closed",
                    tint = Color.LightGray,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    " Ended",
                    maxLines = 2
                )
            }
        }
        PollOptions(poll)
    }
}

@Composable
fun PollOptions(poll: PollState) {
    poll.options.forEach {
        val selected = poll.chosenOption == it.index
        val percentage = if (it.count == 0) 0f else (poll.totalCount / it.count.toFloat()) * 100
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 5.dp, start = 5.dp, end = 5.dp)
            .clip(RoundedCornerShape(5.dp))
            .border(
                if (selected) Dp.Hairline else Dp.Unspecified,
                color = AppTheme.colors.highlight,
                shape = RoundedCornerShape(5.dp)
            )
            .background(color = if (selected) AppTheme.colors.backgroundLighter else AppTheme.colors.backgroundMedium)
            .clickable(enabled = !poll.closed) { poll.vote(it) }
        ) {
            Text(
                it.name, modifier = Modifier.padding(start = 10.dp),
                maxLines = 2
            )
            Spacer(Modifier.weight(1f))
            Text(
                "${percentage}% (${it.count})",
                modifier = Modifier.padding(end = 10.dp),
                maxLines = 2,
            )
        }
    }
}