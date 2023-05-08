package ui.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.LoadState

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MessageInputView(chat: ChatState) {
    val model = chat.messageInput
    val coroutineScope = rememberCoroutineScope()
    val label =
        if (chat.connectionStatus.currentUser == null && chat.connectionStatus.currentChannel != null) "Guest login"
        else "Send a message"

    var loadState by remember { mutableStateOf<LoadState<Unit>>(LoadState.Success(Unit)) }
    val isInputEnabled =
        remember { derivedStateOf { loadState !is LoadState.Loading && chat.connectionStatus.currentChannel != null } }

    Column {
        OutlinedTextField(
            value = model.message,
            onValueChange = {
                model.message = it
                model.completionContext = null
            },
            label = { Text(text = label) },
            enabled = isInputEnabled.value,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp)
                .onPreviewKeyEvent {
                    when {
                        it.key == Key.Enter && it.type == KeyEventType.KeyDown -> {
                            if (chat.connectionStatus.connectedAndAuthenticated()) {
                                chat.sendMessage(model.message())
                                model.setMessage("")
                            } else coroutineScope.launch {
                                loadState = LoadState.Loading()
                                loadState = try {
                                    chat.login(model.message())
                                    LoadState.Success(Unit)
                                } catch (e: Exception) {
                                    LoadState.Error(e)
                                }
                                model.setMessage("")
                            }

                            true
                        }

                        it.key == Key.DirectionUp && it.type == KeyEventType.KeyDown -> {
                            val lastIndex = model.lastArrowCompletionIndex
                            if (lastIndex == null) {
                                if (model.sentMessages.isNotEmpty()) {
                                    val index = model.sentMessages.size - 1
                                    model.setMessage(model.sentMessages[index])
                                    model.lastArrowCompletionIndex = index
                                }
                            } else if (lastIndex > 0) {
                                model.setMessage(model.sentMessages[lastIndex - 1])
                                model.lastArrowCompletionIndex = lastIndex - 1
                            }
                            true
                        }

                        it.key == Key.DirectionDown && it.type == KeyEventType.KeyDown -> {
                            val currentIndex = model.lastArrowCompletionIndex
                            if (currentIndex != null && currentIndex < model.sentMessages.size - 1) {
                                model.setMessage(model.sentMessages[currentIndex + 1])
                                model.lastArrowCompletionIndex = currentIndex + 1
                            }
                            true
                        }

                        it.key == Key.Tab && it.type == KeyEventType.KeyUp -> {
                            true
                        }

                        it.key == Key.Tab && it.type == KeyEventType.KeyDown -> {
                            model.tabCompletion(chat.completionOptions())
                            true
                        }

                        else -> false
                    }
                }
        )
        LaunchedEffect(loadState) {
            if (loadState is LoadState.Error) {
                delay(5000)
                loadState = LoadState.Success(Unit)
            }
        }

        when (val state = loadState) {
            is LoadState.Error -> Text(
                text = "${state.exception.message}",
                style = TextStyle(
                    color = MaterialTheme.colors.error,
                    fontWeight = FontWeight.Bold
                ),
            )

            is LoadState.Loading -> {}
            is LoadState.Success -> {}
        }
    }
}
