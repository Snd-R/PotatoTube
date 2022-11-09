package org.snd.ui.chat

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.snd.ui.common.AppTheme
import org.snd.ui.image.EmoteImage

@Composable
fun EmoteMenuView(chat: Chat) {
    val allEmotes = remember { chat.channelEmotes.values.toList().sortedBy { it.name.lowercase() } }
    Column {
        var text by remember { mutableStateOf("") }
        var emotes by remember { mutableStateOf(allEmotes) }
        OutlinedTextField(
            value = text,
            label = { Text("search") },
            modifier = Modifier.padding(start = 10.dp),
            onValueChange = {
                text = it
                emotes = allEmotes.filter { emote -> emote.name.contains(it) }
            }
        )
        Spacer(Modifier.size(1.dp))

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 100.dp),
            contentPadding = PaddingValues(10.dp),
        ) {
            items(emotes.size) {
                Emote(emotes[it], chat)
            }
        }

    }
}

@Composable
fun Emote(emote: Chat.Emote, chat: Chat) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .border(width = 1.dp, color = AppTheme.colors.backgroundLight)
            .height(100.dp)
            .width(100.dp)
            .clickable {
                val emoteName = emote.name
                if (chat.messageInput.message.text.isNotBlank()) chat.messageInput.appendToMessage(" $emoteName")
                else chat.messageInput.setMessage(emoteName)
            },
    ) {
        EmoteImage(emote, chat)
    }
}