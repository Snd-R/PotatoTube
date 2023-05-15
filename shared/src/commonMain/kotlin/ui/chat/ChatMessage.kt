package ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import image.ImageLoader
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import org.jsoup.parser.Parser
import ui.chat.ChatState.Message.*
import ui.chat.ChatState.Message.ConnectionMessage.ConnectionType.DISCONNECTED
import ui.common.AppTheme
import java.time.ZoneOffset.UTC
import java.time.format.DateTimeFormatter

@Composable
fun ChatMessage(
    fontSize: TextUnit,
    emoteSize: TextUnit,
    index: Int,
    message: ChatState.Message,
    model: ChatState,
) {
    when (message) {
        is UserMessage -> UserMessageView(
            fontSize,
            emoteSize,
            index,
            message,
            model,
        )

        is ConnectionMessage -> ConnectionMessage(fontSize, message)
        is AnnouncementMessage -> AnnouncementMessageView((fontSize.value + 5f).sp, message)
        is SystemMessage -> SystemMessageView(((fontSize.value - 1f).coerceAtLeast(3f)).sp, message)
    }
}

@Composable
fun UserMessageView(
    fontSize: TextUnit,
    emoteSize: TextUnit,
    index: Int,
    message: UserMessage,
    model: ChatState,
) {
    val channelEmotes by model.channelEmotes.collectAsState()
    val parsedMessage = remember(model.settings.timestampFormat) {
        parseMessage(
            message,
            model.settings.timestampFormat,
            channelEmotes,
            model.connectionStatus.currentUser
        )
    }

    val customEmotesState by model.customEmotes.collectAsState()
    val customEmotes = remember {
        parsedMessage.customEmotes.map {
            customEmotesState[it.url]
                ?: it.also { model.updateCustomEmote(it) }
        }
    }

    // inline placeholder must be of fixed size but the size of an image is unknown prior to load
    // messageDimension state is used to resize placeholder after image was loaded
    // this causes double recomposition on load if dimension state was changed
    // image loader should remember loaded image by provided maxHeight and maxWidth to avoid potentially expensive image reload
    val emotesHeightState by remember { derivedStateOf { (parsedMessage.emotes + customEmotes).map { it.messageDimensions.height } } }
    val inlineContent = remember(emoteSize, emotesHeightState) {
        emoteInlineContent(parsedMessage.emotes + customEmotes, emoteSize, model.imageLoader)
    }

    val backgroundColor =
        if (parsedMessage.isMentioned) AppTheme.colors.backgroundLighter
        else if (index % 2 == 0) AppTheme.colors.backgroundMedium
        else AppTheme.colors.backgroundDark

    Box(
        modifier = Modifier
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

@Composable
fun SystemMessageView(
    fontSize: TextUnit,
    message: SystemMessage,
) {
    Text(
        text = message.message,
        fontSize = fontSize,
        fontWeight = FontWeight.Bold,
        color = AppTheme.colors.systemMessageColor,
        modifier = Modifier.padding(3.dp)
    )
}

@Composable
fun ConnectionMessage(
    fontSize: TextUnit,
    message: ConnectionMessage,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.dp, color = AppTheme.colors.backgroundMedium),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message.message,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            color = if (message.type == DISCONNECTED) MaterialTheme.colors.error else AppTheme.colors.green,
            modifier = Modifier.padding(3.dp)
        )
    }
}


//TODO no selectable and clickable text for now https://github.com/JetBrains/compose-jb/issues/1450
fun parseMessage(
    message: UserMessage,
    timestampFormat: String,
    channelEmotes: Map<String, ChatState.Emote>,
    currentUser: String?
): ParsedMessage {
    val messageDocument = Jsoup.parse(message.message, Parser.xmlParser())
    val timestamp = timestampFormat.ifBlank { null }?.let {
        DateTimeFormatter.ofPattern(it)
            .withZone(UTC)
            .format(message.timestamp)
    }

    val customEmotes = mutableSetOf<ChatState.Emote>()
    val emotes = mutableSetOf<ChatState.Emote>()
    var isMentioned = false

    val annotatedString = buildAnnotatedString {
        timestamp?.let { append("$it ") }
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
                        customEmotes.add(ChatState.Emote(name = url, url = url))
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
    emotes: Collection<ChatState.Emote>,
    emoteSize: TextUnit,
    imageLoader: ImageLoader,
): Map<String, InlineTextContent> = emotes.associate { emote ->
    emote.name to InlineTextContent(
        Placeholder(
            emote.messageDimensions.width?.sp ?: emoteSize,
            emote.messageDimensions.height?.sp ?: emoteSize,
            PlaceholderVerticalAlign.Center
        )
    ) {
        imageLoader.LoadEmoteImage(
            emote = emote,
            emoteDimensions = emote.messageDimensions,
            maxHeight = emoteSize.value.toInt(),
            maxWidth = emoteSize.value.toInt()
        )
    }
}

fun AnnotatedString.Builder.processAndAppendMessage(
    text: String,
    channelEmotes: Map<String, ChatState.Emote>,
    currentUser: String?
): TextMessageResult {
    var isMentioned = false
    val emotes = mutableSetOf<ChatState.Emote>()

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
    val emotes: Collection<ChatState.Emote>,
    val customEmotes: Collection<ChatState.Emote>
)

data class TextMessageResult(
    val isMentioned: Boolean,
    val emotes: Collection<ChatState.Emote>,
)
