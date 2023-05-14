package ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.LoadState

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChannelSettings(settings: SettingsState) {
    val coroutineScope = rememberCoroutineScope()
    var channelName by remember { mutableStateOf("") }
    var channelLoadState by remember { mutableStateOf<LoadState<Unit>?>(null) }

    fun joinChannel() {
        if (channelName.isNotBlank())
            coroutineScope.launch {
                channelLoadState = LoadState.Loading()

                channelLoadState = try {
                    settings.changeChannel(channelName)
                    LoadState.Success(Unit)
                } catch (e: Exception) {
                    LoadState.Error(e)
                }
                channelName = ""
            }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Channel Settings")
        val currentChannelText = settings.channel?.let { "Current channel: ${settings.channel}" }
            ?: "Current channel is not set"
        Text(currentChannelText)

        if (settings.channel != null) {
            Button(onClick = {
                settings.disconnect()
            }) {
                Text("Disconnect")
            }
        }

        Divider()
        Row(modifier = Modifier.fillMaxWidth()) {
            TextField(
                value = channelName,
                label = { Text(text = "new channel") },
                onValueChange = { channelName = it.trim() },
                modifier = Modifier.weight(1f)
                    .onPreviewKeyEvent {
                        when {
                            it.key == Key.Enter && it.type == KeyEventType.KeyDown -> {
                                joinChannel()
                                true
                            }

                            else -> false
                        }
                    }
            )
            Spacer(Modifier.size(10.dp))
            Button(
                onClick = { joinChannel() }) {
                Text("Connect")
            }
        }

        when (val state = channelLoadState) {
            null -> {}
            is LoadState.Loading -> {
                settings.isLoading = true
            }

            is LoadState.Success -> {
                channelLoadState = null
                settings.isLoading = false
            }

            is LoadState.Error -> {
                Text(
                    text = state.exception.message ?: "Error",
                    style = TextStyle(color = MaterialTheme.colors.error, fontWeight = FontWeight.Bold),
                )
                settings.isLoading = false
            }
        }
    }
}
