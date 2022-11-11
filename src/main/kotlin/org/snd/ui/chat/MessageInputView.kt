package org.snd.ui.chat

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import java.util.*

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MessageInputView(
    chat: Chat,
//    options: List<String>,
    sendMessage: (message: String) -> Unit
) {
    val model = chat.messageInput
    OutlinedTextField(
        value = model.message,
        onValueChange = {
            model.message = it
            model.completionContext = null
        },
        label = { Text(text = "Send a message") },
        modifier = Modifier
            .fillMaxWidth()
            .onPreviewKeyEvent {
                when {
                    it.key == Key.Enter && it.type == KeyEventType.KeyDown -> {
                        sendMessage(model.message())
                        model.setMessage("")
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
//                            val index = (currentIndex - 1).coerceAtLeast(0)
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

                    // TODO implement workaround for ctrl + backspace on empty textfield https://github.com/JetBrains/compose-jb/issues/565 - still not fixed
//                    it.isCtrlPressed && it.key == Key.Backspace && it.type == KeyEventType.KeyDown -> {
//                        true
//                    }

                    else -> false
                }
            }
    )
}
