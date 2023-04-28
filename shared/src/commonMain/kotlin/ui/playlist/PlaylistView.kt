package ui.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.LoadState
import platform.Tooltip
import ui.common.AppTheme

@Composable
fun PlaylistView(model: Playlist) {
    Box(modifier = Modifier.fillMaxWidth()) {
        val scrollState = rememberLazyListState()
        Column {
            AddToQueueButton(model)
            PlaylistHeader(model)
            LazyColumn(state = scrollState) {
                items(model.items.size) {
                    PlaylistItem(model.items[it], it == 0)
                }
            }
        }
    }
}

@Composable
fun PlaylistItem(item: PlaylistItem, active: Boolean) {
    val queuedBy = item.queueBy.ifEmpty { "Unknown" }
    val link = item.media.getLink()
    val clipboardManager = LocalClipboardManager.current
    Tooltip(
        tooltip = {
            Text(
                "added by $queuedBy. $link", modifier = Modifier
                    .background(color = AppTheme.colors.backgroundLight)
            )
        },
        delayMillis = 300
    ) {
        Row(
            modifier = Modifier
                .border(width = 1.dp, color = Color.LightGray)
                .fillMaxWidth()
                .background(color = if (active) AppTheme.colors.backgroundLighter else AppTheme.colors.backgroundMedium)
        ) {
            Text("${item.media.title} - ${item.media.duration}", modifier = Modifier.padding(5.dp))
            Spacer(Modifier.weight(1f))
            Icon(
                Icons.Default.ContentCopy,
                contentDescription = "copy",
                tint = Color.White,
                modifier = Modifier
                    .width(25.dp)
                    .height(25.dp)
                    .align(Alignment.CenterVertically)
                    .clickable {
                        clipboardManager.setText(AnnotatedString(link))
                    }
            )
        }
    }

}

@Composable
fun PlaylistHeader(model: Playlist) {
    Box(
        modifier = Modifier
            .border(width = 1.dp, color = Color.LightGray)
            .padding(start = 3.dp)
            .fillMaxWidth()
    ) {
        val itemsText = if (model.count == 1) "item" else "items"
        Text("${model.count} $itemsText - ${model.time}")
    }
}

@Composable
fun AddToQueueButton(model: Playlist) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 3.dp)
    ) {
        var showInput by remember { mutableStateOf(false) }
        Icon(
            Icons.Default.Add,
            contentDescription = "Add",
            tint = if (model.locked) MaterialTheme.colors.error else Color.White,
            modifier = Modifier
                .width(30.dp)
                .height(30.dp)
                .clickable(
                    enabled = !model.locked
                ) {
                    showInput = !showInput
                }
        )

        var text by remember { mutableStateOf("") }
        val coroutineScope = rememberCoroutineScope()
        var queueAddState by remember { mutableStateOf<LoadState<Unit>?>(null) }

        if (showInput && !model.locked) {
            OutlinedTextField(
                value = text,
                label = { Text("Media URL", fontSize = 14.sp) },
                onValueChange = {
                    text = it
                },
                textStyle = TextStyle(fontSize = 14.sp),
                modifier = Modifier
                    .widthIn(max = 300.dp)
            )
            Button(onClick = {
                queueAddState = LoadState.Loading()
                coroutineScope.launch {
                    queueAddState = model.cytube.queue(url = text, putLast = true, temp = true)
                    text = ""
                    showInput = false
                }
            }) {
                Text("Queue Last")
            }
        }

        when (val currentstate = queueAddState) {
            null -> {}
            is LoadState.Error -> Text(
                text = currentstate.exception.message ?: "Error",
                style = TextStyle(color = MaterialTheme.colors.error, fontWeight = FontWeight.Bold),
            )

            is LoadState.Loading -> CircularProgressIndicator()

            is LoadState.Success -> queueAddState = null
        }
    }
}