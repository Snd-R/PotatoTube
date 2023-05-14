package ui.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cytube.CytubeClient
import image.ImageLoader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import ui.ConnectionStatus
import ui.poll.PollState
import ui.settings.SettingsState
import java.time.Instant

class ChatState(
    private val cytube: CytubeClient,
    val settings: SettingsState,
    val connectionStatus: ConnectionStatus,
    val imageLoader: ImageLoader,
    val poll: PollState,
) {
    val lastUserMessageTimestamp = MutableStateFlow<Instant?>(null)
    val messages = MutableStateFlow(listOf<Message>())
    val channelEmotes = MutableStateFlow(emptyMap<String, Emote>())
    val customEmotes = MutableStateFlow(emptyMap<String, Emote>())

    var users = Users()
    val messageInput = MessageInputState()

    var scrolledUp = MutableStateFlow(false)
    var showUserList by mutableStateOf(false)

    var isLoading by mutableStateOf(true)

    fun addUserMessage(message: Message.UserMessage) {
        val lastTimestamp = lastUserMessageTimestamp.value
        if (lastTimestamp != null && !message.timestamp.isAfter(lastTimestamp)) return

        addMessage(message)
        this.lastUserMessageTimestamp.value = message.timestamp
    }

    fun addAnnouncementMessage(message: Message.AnnouncementMessage) {
        addMessage(message)
    }

    fun addSystemMessage(message: String) {
        addMessage(Message.SystemMessage(message))
    }

    fun addConnectionMessage(message: Message.ConnectionMessage) {
        addMessage(message)
    }

    private fun addMessage(message: Message) {
        val messages = messages.value
        val dropSize = (messages.size - settings.historySize).coerceAtLeast(0)

        this.messages.value = messages.drop(dropSize).plus(message)
    }

    fun sendMessage(message: String) {
        cytube.sendMessage(message)
        messageInput.sentMessages.add(message)
        messageInput.lastArrowCompletionIndex = null
    }

    fun reset() {
        messages.value = emptyList()
        lastUserMessageTimestamp.value = null
        channelEmotes.value = emptyMap()
        users = Users()
    }

    fun completionOptions(): List<String> {
        val firstWord = messageInput.message().trim().none { char -> char.isWhitespace() }
        val userList = users.users.value.map { it.name }.toList()
        val userOptions = if (firstWord) userList.map { user -> "$user:" } else userList
        return userOptions + channelEmotes.value.keys
    }

    fun setEmotes(emotes: List<Emote>) {
        channelEmotes.value = emotes.associateBy { it.name }
    }

    fun updateEmote(emote: Emote) {
        val channelEmotes = channelEmotes.value
        if (channelEmotes.containsKey(emote.name))
            addSystemMessage("Emote ${emote.name} was updated")
        else
            addSystemMessage("Emote ${emote.name} was added")

        val newMap = channelEmotes.toMutableMap()
        newMap[emote.name] = emote
        this.channelEmotes.value = newMap
    }

    fun updateCustomEmote(emote: Emote) {
        val customEmotes = customEmotes.value

        val newMap = customEmotes.toMutableMap()
        newMap[emote.url] = emote
        this.customEmotes.value = newMap
    }

    fun removeEmote(emote: Emote) {
        val channelEmotes = channelEmotes.value
        if (channelEmotes.containsKey(emote.name))
            addSystemMessage("Emote ${emote.name} was removed")

        val newMap = channelEmotes.toMutableMap()
        newMap.remove(emote.name)
        this.channelEmotes.value = newMap
    }

    fun toggleUserList() {
        this.showUserList = !this.showUserList
    }

    fun setScrollState(state: Boolean) {
        this.scrolledUp.value = state
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
        data class SystemMessage(val message: String) : Message()

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
        val messageDimensions = EmoteDimensions()
        val emoteMenuDimensions = EmoteDimensions()
    }

    class EmoteDimensions {
        var height by mutableStateOf<Int?>(null)
        var width by mutableStateOf<Int?>(null)
    }

    class User(
        val name: String,
        rank: Double,
        afk: Boolean,
        muted: Boolean
    ) {
        var afk = MutableStateFlow(afk)
        var muted = MutableStateFlow(muted)
        var rank = MutableStateFlow(
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
        var users = MutableStateFlow(emptyList<User>())
        var siteAdmins = MutableStateFlow(0)
        var channelAdmins = MutableStateFlow(0)
        var moderators = MutableStateFlow(0)
        var regularUsers = MutableStateFlow(0)
        var guests = MutableStateFlow(0)
        var anonymous = MutableStateFlow(0)
        var afk = MutableStateFlow(0)
        var userCount = MutableStateFlow(0)

        fun setUsers(newUsers: List<User>) {
            users.value = newUsers.sortedByDescending { it.rank.value.rank }
            newUsers.forEach { updateUserCountsWith(it, true) }
        }

        fun userCount(userCount: Int) {
            this.userCount.value = userCount
            this.anonymous.value = userCount - this.users.value.size
        }

        fun setAfk(user: String, afk: Boolean) {
            users.value
                .firstOrNull { it.name == user }
                ?.afk?.value = afk
        }

        fun addUser(user: User) {
            val newUsers = users.value.plus(user).sortedByDescending { it.rank.value.rank }
            users.value = newUsers
            updateUserCountsWith(user, true)
        }

        fun removeUser(name: String) {
            val users = users.value.toMutableList()
            val userIndex = users.indexOfFirst { it.name == name }
            if (userIndex == -1) return

            val user = users[userIndex]
            updateUserCountsWith(user, false)
            users.removeAt(userIndex)

            this.users.value = users
        }

        private fun updateUserCountsWith(user: User, positive: Boolean) {
            val amount = if (positive) 1 else -1
            when (user.rank.value) {
                UserRank.GUEST -> guests.update { it + amount }
                UserRank.REGULAR_USER -> regularUsers.update { it + amount }
                UserRank.MODERATOR -> moderators.update { it + amount }
                UserRank.CHANNEL_ADMIN -> channelAdmins.update { it + amount }
                UserRank.SITE_ADMIN -> siteAdmins.update { it + amount }
            }
            if (user.afk.value) afk.update { it + amount }
            this.anonymous.value = userCount.value - users.value.size

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