package org.snd.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import org.jsoup.parser.Parser
import org.snd.image.Dimension
import org.snd.ui.chat.Chat.Message.AnnouncementMessage
import org.snd.ui.chat.Chat.Message.UserMessage
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
) {
    when (message) {
        is UserMessage -> UserMessageView(
            fontSize,
            emoteSize,
            height,
            index,
            message,
            model,
        )

        is AnnouncementMessage -> AnnouncementMessageView((fontSize.value + 5f).sp, message)

    }
}

@Composable
fun UserMessageView(
    fontSize: TextUnit,
    emoteSize: TextUnit,
    height: Dp,
    index: Int,
    message: UserMessage,
    model: Chat,
) {

    val parsedMessage = remember(model.settings.timestampFormat) {
        parseMessage(
            message,
            model.settings.timestampFormat,
            model.channelEmotes,
            model.userStatus.currentUser
        )
    }

    val customEmotes = remember {
        parsedMessage.customEmotes.map {
            model.customEmotes[it.url]
                ?: it.also { model.customEmotes[it.url] = it }
        }
    }

    val inlineContent = remember {
        emoteInlineContent(
            parsedMessage.emotes + customEmotes,
            emoteSize,
            model
        )
    }

    val backgroundColor =
        if (parsedMessage.isMentioned) AppTheme.colors.backgroundLighter
        else if (index % 2 == 0) AppTheme.colors.backgroundMedium
        else AppTheme.colors.backgroundDark

    Box(
        modifier = Modifier
            .heightIn(min = height)
            .fillMaxWidth()
            .background(backgroundColor)
    ) {
        Text(
            parsedMessage.annotatedString,
            inlineContent = inlineContent,
            softWrap = true,
            fontSize = fontSize,
            modifier = Modifier.padding(3.dp)
        )
    }
}

@Composable
fun AnnouncementMessageView(
    fontSize: TextUnit,
    message: AnnouncementMessage,
) {
    Text(
        text = message.message,
        fontSize = fontSize,
        fontWeight = FontWeight.Bold,
        color = AppTheme.colors.announcementMessageColor,
        modifier = Modifier.padding(3.dp)
    )

}

//TODO no selectable and clickable text for now https://github.com/JetBrains/compose-jb/issues/1450
fun parseMessage(
    message: UserMessage,
    timestampFormat: String,
    channelEmotes: Map<String, Chat.Emote>,
    currentUser: String?
): ParsedMessage {
    val messageDocument = Jsoup.parse(message.message, Parser.xmlParser())
    val timestamp = DateTimeFormatter.ofPattern(timestampFormat)
        .withZone(UTC)
        .format(message.timestamp)

    val customEmotes = mutableSetOf<Chat.Emote>()
    val emotes = mutableSetOf<Chat.Emote>()
    var isMentioned = false

    val annotatedString = buildAnnotatedString {
        append("[$timestamp] ")
        withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) { append(message.user) }
        append(":")

        messageDocument.childNodes().forEach { node ->
            if (node is Element) {
                when (node.tagName()) {
                    "a" -> {
                        val text = node.text()
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

                    "img" -> {
                        val url = node.attr("src")
                        appendInlineContent(id = url, alternateText = url)
                        customEmotes.add(Chat.Emote(name = url, url = url))
                    }

                    else -> processAndAppendMessage(node.text(), channelEmotes, currentUser)
                        .also {
                            emotes.addAll(it.emotes)
                            isMentioned = it.isMentioned
                        }
                }
            } else if (node is TextNode) {
                processAndAppendMessage(node.text(), channelEmotes, currentUser)
                    .also {
                        emotes.addAll(it.emotes)
                        isMentioned = it.isMentioned
                    }
            }
        }
    }
    return ParsedMessage(
        annotatedString = annotatedString,
        isMentioned = isMentioned,
        emotes = emotes,
        customEmotes = customEmotes
    )
}

fun emoteInlineContent(
    emotes: Collection<Chat.Emote>,
    emoteSize: TextUnit,
    model: Chat
): Map<String, InlineTextContent> = emotes.associate { emote ->
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

fun AnnotatedString.Builder.processAndAppendMessage(
    text: String,
    channelEmotes: Map<String, Chat.Emote>,
    currentUser: String?
): TextMessageResult {
    var isMentioned = false
    val emotes = mutableSetOf<Chat.Emote>()

    text.split(" ").forEach { token ->
        val emote = channelEmotes[token]
        if (emote != null) {
            append(" ")
            appendInlineContent(id = emote.name, alternateText = emote.name)
            emotes.add(emote)
        } else {
            if (currentUser != null && (token == currentUser || token == "$currentUser:"))
                isMentioned = true
            append(" $token")
        }
    }

    return TextMessageResult(isMentioned, emotes)
}

data class ParsedMessage(
    val annotatedString: AnnotatedString,
    val isMentioned: Boolean,
    val emotes: Collection<Chat.Emote>,
    val customEmotes: Collection<Chat.Emote>
)

data class TextMessageResult(
    val isMentioned: Boolean,
    val emotes: Collection<Chat.Emote>,
)
