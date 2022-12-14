package org.snd.ui.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.snd.cytube.CytubeClient
import org.snd.image.ImageLoader
import org.snd.ui.ConnectionStatus
import org.snd.ui.poll.PollState
import org.snd.ui.settings.SettingsModel
import java.time.Instant

class Chat(
    private val cytube: CytubeClient,
    val settings: SettingsModel,
    val connectionStatus: ConnectionStatus,
    val imageLoader: ImageLoader,
    val poll: PollState,
) {
    var scrolledUp by mutableStateOf(false)
    val messages: MutableList<Message> = mutableStateListOf()
    var lastUserMessageTimestamp by mutableStateOf<Instant?>(null)
    val channelEmotes = mutableStateMapOf<String, Emote>()
    val customEmotes = mutableStateMapOf<String, Emote>()
    var users = Users()

    val messageInput = MessageInput()

    fun addUserMessage(message: Message.UserMessage) {
        val lastTimestamp = lastUserMessageTimestamp
        if (lastTimestamp != null && !message.timestamp.isAfter(lastTimestamp)) return

        addMessage(message)
        this.lastUserMessageTimestamp = message.timestamp
    }

    fun addAnnouncementMessage(message: Message.AnnouncementMessage) {
        addMessage(message)
    }

    fun addConnectionMessage(message: Message.ConnectionMessage) {
        addMessage(message)
    }

    private fun addMessage(message: Message) {
        while (messages.size >= settings.historySize) messages.removeFirstOrNull()
        messages.add(message)
    }

    fun sendMessage(message: String) {
        cytube.sendMessage(message)
        messageInput.sentMessages.add(message)
        messageInput.lastArrowCompletionIndex = null
    }

    fun reset() {
        messages.clear()
        lastUserMessageTimestamp = null
        channelEmotes.clear()
        users = Users()
    }

    fun completionOptions(): List<String> {
        val firstWord = messageInput.message().trim().none { char -> char.isWhitespace() }
        val userList = users.users.map { it.name }.toList()
        val userOptions = if (firstWord) userList.map { user -> "$user:" } else userList
        return userOptions + channelEmotes.keys
    }

    fun setEmotes(emotes: List<Emote>) {
        channelEmotes.clear()
        emotes.forEach { emote -> channelEmotes[emote.name] = emote }
    }

    suspend fun login(username: String) {
        cytube.login(username, null)
    }

    sealed class Message {

        data class UserMessage(
            val user: String,
            val message: String,
            val timestamp: Instant
        ) : Message()

        data class AnnouncementMessage(val message: String) : Message()

        data class ConnectionMessage(
            val message: String,
            val type: ConnectionType
        ) : Message() {

            enum class ConnectionType {
                CONNECTED,
                DISCONNECTED
            }
        }
    }


    class Emote(
        val name: String,
        val url: String,
    ) {
        val messageDimension = EmoteDimension()
        val emoteMenuDimension = EmoteDimension()
    }

    class EmoteDimension {
        var height by mutableStateOf<Int?>(null)
        var width by mutableStateOf<Int?>(null)
    }

    class User(
        val name: String,
        rank: Double,
        afk: Boolean,
        muted: Boolean
    ) {
        var afk by mutableStateOf(afk)
        var muted by mutableStateOf(muted)
        var rank: UserRank by mutableStateOf(
            when (rank) {
                0.0 -> UserRank.GUEST
                1.0, 1.5 -> UserRank.REGULAR_USER
                2.0 -> UserRank.MODERATOR
                3.0, 5.0, 10.0 -> UserRank.CHANNEL_ADMIN
                255.0 -> UserRank.SITE_ADMIN
                else -> UserRank.GUEST
            }
        )

    }

    class Users {
        var users = mutableStateListOf<User>()
        var siteAdmins by mutableStateOf(0)
        var channelAdmins by mutableStateOf(0)
        var moderators by mutableStateOf(0)
        var regularUsers by mutableStateOf(0)
        var guests by mutableStateOf(0)
        var anonymous by mutableStateOf(0)
        var afk by mutableStateOf(0)
        var userCount by mutableStateOf(0)

        fun setUsers(newUsers: List<User>) {
            users.clear()
            users.addAll(newUsers.sortedByDescending { it.rank.rank })
            newUsers.forEach { updateUserCountsWith(it, true) }
        }

        fun userCount(userCount: Int) {
            this.userCount = userCount
            this.anonymous = userCount - users.size
        }

        fun setAfk(user: String, afk: Boolean) {
            users.firstOrNull { it.name == user }?.afk = afk
        }

        fun addUser(user: User) {
            users.add(user)
            users.sortByDescending { it.rank }
            updateUserCountsWith(user, true)
        }

        fun removeUser(name: String) {
            val user = users.first { it.name == name }
            updateUserCountsWith(user, false)
            users.remove(user)
        }

        private fun updateUserCountsWith(user: User, positive: Boolean) {
            val amount = if (positive) 1 else -1
            when (user.rank) {
                UserRank.GUEST -> guests += amount
                UserRank.REGULAR_USER -> regularUsers += amount
                UserRank.MODERATOR -> moderators += amount
                UserRank.CHANNEL_ADMIN -> channelAdmins += amount
                UserRank.SITE_ADMIN -> siteAdmins += amount
            }
            if (user.afk) afk += amount
            this.anonymous = userCount - users.size

        }
    }

    enum class UserRank(val rank: Int) {
        GUEST(0),
        REGULAR_USER(1),
        MODERATOR(2),
        CHANNEL_ADMIN(3),
        SITE_ADMIN(255),
    }
}