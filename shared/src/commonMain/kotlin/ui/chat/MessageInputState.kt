package ui.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

class MessageInputState {
    var message by mutableStateOf(TextFieldValue(""))
    var sentMessages = mutableStateListOf<String>()

    var completionContext by mutableStateOf<CompletionContext?>(null)
    var lastArrowCompletionIndex by mutableStateOf<Int?>(null)

    fun message() = message.text


    fun appendToMessage(value: String) {
        val text = message.text + value
        message = message.copy(text = "${message.text}$value", selection = TextRange(text.length))
    }

    fun setMessage(value: String) {
        message = message.copy(text = value, selection = TextRange(value.length))
    }

    fun tabCompletion(options: List<String>) {
        val result = completion(
            message.text,
            message.selection.end,
            options,
            completionContext
        )
        message = message.copy(text = result.text, selection = TextRange(result.newPosition))
        completionContext = result.context
    }

    private fun completion(
        input: String,
        position: Int,
        options: List<String>,
        context: CompletionContext?
    ): CompletionResult {
        if (context != null && position > 0) {
            val currentCompletion = input.substring(context.start, position - 1)
            if (currentCompletion == context.matches[context.tabIndex]) {
                val currentIndex = (context.tabIndex + 1) % context.matches.size
                val completed = context.matches[currentIndex]

                return CompletionResult(
                    input.substring(0, context.start) + completed + " " + input.substring(position),
                    context.start + completed.length + 1,
                    context.copy(tabIndex = currentIndex)
                )
            }
        }

        val lower = input.substring(0, position).lowercase()
        var start = 0
        val incomplete = buildString {
            for (index in lower.indices.reversed()) {
                start = index
                if (lower[index].isWhitespace()) {
                    start++
                    break
                }
                append(lower[index])
            }
        }.reversed()

        if (incomplete.isBlank()) {
            return CompletionResult(input, position, null)
        }

        val matches = options.filter { it.lowercase().indexOf(incomplete) == 0 }
            .sortedBy { it.lowercase() }

        if (matches.isEmpty())
            return CompletionResult(input, position, null)

        return CompletionResult(
            text = input.substring(0, start) + matches[0] + " " + input.substring(position),
            newPosition = start + matches[0].length + 1,
            CompletionContext(start = start, matches = matches, tabIndex = 0)
        )
    }

    data class CompletionContext(
        val start: Int,
        val matches: List<String>,
        val tabIndex: Int,
    )

    data class CompletionResult(
        val text: String,
        val newPosition: Int,
        val context: CompletionContext?
    )
}