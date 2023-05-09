package ui.chat

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ui.common.AppTheme
import ui.common.LocalWindowSize
import ui.common.WindowSize

@Composable
fun EmoteMenuView(chat: ChatState) {
    val channelEmotesState by chat.channelEmotes.collectAsState()
    val allEmotes by remember { derivedStateOf { channelEmotesState.values.toList().sortedBy { it.name.lowercase() } } }
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
        val cellSize = when (LocalWindowSize.current) {
            WindowSize.COMPACT, WindowSize.MEDIUM, WindowSize.EXPANDED -> 70.dp
            WindowSize.FULL -> 100.dp
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = cellSize),
            contentPadding = PaddingValues(10.dp),
        ) {
            items(emotes.size, key = { emotes[it].name }) {
                Emote(emotes[it], chat)
            }
        }
    }
}

@Composable
fun Emote(emote: ChatState.Emote, chat: ChatState) {
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
        chat.imageLoader.LoadEmoteImage(
            emote = emote,
            emoteDimensions = emote.emoteMenuDimensions,
            maxHeight = 100,
            maxWidth = 100
        )
    }
}