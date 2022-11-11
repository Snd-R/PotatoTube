package org.snd.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
    println("composing message $message")
    val messageDocument = Jsoup.parse(message.message, Parser.xmlParser())

    val timestamp = DateTimeFormatter.ofPattern(timestampFormat)
        .withZone(UTC)
        .format(message.timestamp)

    val customEmotes = remember { mutableSetOf<Chat.Emote>() }
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

    // we don't know emote dimensions since it's not loaded yet, but we have to set placeholder size
    // after emote is loaded we store emote dimensions and recompose with proper placeholder size
    val inlineContentMap = (model.channelEmotes.values + customEmotes).associate { emote ->
        val emoteHeight = emote.height
        val emoteWidth = emote.width
        val (actualWidth, actualHeight) = if (emoteHeight != null && emoteWidth != null) {
            scaleImageDimension(
                from = Dimension(width = emoteWidth, height = emoteHeight),
                to = Dimension(emoteSize.value, emoteSize.value)
            )
        } else Dimension(emoteSize.value, emoteSize.value)
        emote.name to InlineTextContent(Placeholder(actualWidth.sp, actualHeight.sp, PlaceholderVerticalAlign.Center)) {
            EmoteImage(emote, model)
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
    appendInlineContent(id = url, alternateText = "Custom Emote")
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

fun scaleImageDimension(from: Dimension, to: Dimension): Dimension {
    val bestRatio = (to.width / from.width).coerceAtMost(from.height / to.height)
    return Dimension(
        width = from.width * bestRatio,
        height = from.height * bestRatio
    )
}