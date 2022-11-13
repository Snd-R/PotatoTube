package org.snd.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import org.jsoup.parser.Parser
import org.snd.image.Dimension
import org.snd.ui.common.AppTheme
import org.snd.ui.image.EmoteImage
import java.time.ZoneOffset.UTC
import java.time.format.DateTimeFormatter

@Composable
fun ChatMessage(
    fontSize: TextUnit,
    emoteSize: TextUnit,
    height: Dp,
    index: Int,
    message: Chat.Message,
    model: Chat,
    timestampFormat: String,
) {
    val messageDocument = Jsoup.parse(message.message, Parser.xmlParser())

    val timestamp = DateTimeFormatter.ofPattern(timestampFormat)
        .withZone(UTC)
        .format(message.timestamp)

    val customEmotes = mutableSetOf<Chat.Emote>()
    val annotatedString = buildAnnotatedString {
        append("[$timestamp] ")
        withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
            append(message.user)
        }
        append(":")

        messageDocument.childNodes().forEach { node ->
            if (node is Element) {
                when (node.tagName()) {
                    "a" -> appendLink(node.text())  //TODO no selectable and clickable text for now https://github.com/JetBrains/compose-jb/issues/1450
                    "img" -> {
                        val emote = appendCustomEmote(node.attr("src"))
                        customEmotes.add(emote)
                    }

                    else -> {
                        appendMessage(node.text(), model.channelEmotes)
                    }
                }
            } else if (node is TextNode) {
                appendMessage(node.text(), model.channelEmotes)
            }
        }
    }
    val messageCustomEmotes = customEmotes.map {
        model.customEmotes[it.url]
            ?: (it.also { model.customEmotes[it.url] = it })
    }

    val inlineContentMap = (model.channelEmotes.values + messageCustomEmotes).associate { emote ->
        emote.name to InlineTextContent(
            Placeholder(
                emote.messageDimension.width?.sp ?: emoteSize,
                emote.messageDimension.height?.sp ?: emoteSize,
                PlaceholderVerticalAlign.Center
            )
        ) {
            EmoteImage(
                emote,
                emote.messageDimension,
                model,
                scaleTo = Dimension(
                    height = emoteSize.value.toInt(),
                    width = emoteSize.value.toInt()
                )
            )

        }
    }

    Box(
        modifier = Modifier
            .heightIn(min = height)
            .fillMaxWidth()
            .background((if (index % 2 == 0) AppTheme.colors.backgroundMedium else AppTheme.colors.backgroundDark))
    ) {
        Text(
            annotatedString,
            inlineContent = inlineContentMap,
            softWrap = true,
            fontSize = fontSize,
        )
    }
}

fun AnnotatedString.Builder.appendCustomEmote(url: String): Chat.Emote {
    appendInlineContent(id = url, alternateText = url)
    return Chat.Emote(name = url, url = url)
}

fun AnnotatedString.Builder.appendMessage(text: String, emotes: Map<String, Chat.Emote>) {
    text.split(" ").map { token ->
        val emote = emotes[token]
        if (emote != null) {
            append(" ")
            appendInlineContent(id = emote.name, alternateText = emote.name)
        } else append(" $token")
    }
}

fun AnnotatedString.Builder.appendLink(text: String) {
    pushStyle(
        SpanStyle(
            color = Color.Companion.LightGray,
            textDecoration = TextDecoration.Underline
        )
    )
    pushStringAnnotation(tag = "URL", annotation = text)
    append(text)
    pop()
    pop()
}