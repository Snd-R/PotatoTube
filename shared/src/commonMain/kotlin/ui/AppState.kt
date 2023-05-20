package ui

import cytube.CytubeClient
import cytube.CytubeEventHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ui.channel.ChannelState
import ui.chat.ChatState
import ui.home.HomePageState
import ui.playlist.PlaylistItem
import ui.poll.Poll
import ui.settings.SettingsState

class AppState(
    val channel: ChannelState,
    val homePage: HomePageState,
    val settings: SettingsState,
    val connectionStatus: ConnectionStatus,
    cytubeClient: CytubeClient
) {
    init {
        cytubeClient.addEventListener(EventHandler())
    }

    inner class EventHandler : CytubeEventHandler {
        private val coroutineScope = CoroutineScope(Dispatchers.IO)

        override fun onChatMessage(message: ChatState.Message.UserMessage) {
            channel.chat.addUserMessage(message)
        }

        override fun onLoginSuccess(name: String, isGuest: Boolean) {
            connectionStatus.currentUser.value = name
            connectionStatus.isGuest.value = isGuest
        }

        override fun onRank(rank: Double) {
            connectionStatus.rank.value = rank
        }

        override fun onEmoteList(emotes: List<ChatState.Emote>) {
            channel.chat.setEmotes(emotes)
        }

        override fun onUserList(users: List<ChatState.User>) {
            channel.chat.users.setUsers(users)
        }

        override fun onUserCount(count: Int) {
            channel.chat.users.userCount(count)
        }

        override fun onSetAfk(username: String, afk: Boolean) {
            channel.chat.users.setAfk(username, afk)
        }

        override fun onAddUser(user: ChatState.User) {
            channel.chat.addUser(user)
        }

        override fun onUserLeave(username: String) {
            channel.chat.onUserLeave(username)
        }

        override fun onChangeMedia(url: String) {
            channel.player.setMrl(url)
        }

        override fun onMediaUpdate(timeMillis: Long, paused: Boolean) {
            channel.player.sync(timeMillis, paused)
        }

        override fun onQueue(item: PlaylistItem, position: String) {
            if (position == "prepend")
                channel.playlist.addFirst(item)
            else channel.playlist.addItem(item, position.toInt())
        }

        override fun onPlaylist(items: List<PlaylistItem>) {
            channel.playlist.setPlaylist(items)
        }

        override fun onPlaylistMeta(rawTime: Long, count: Int, time: String) {
            channel.playlist.rawTime.value = rawTime
            channel.playlist.count.value = count
            channel.playlist.time.value = time
        }

        override fun onDeletePlaylistItem(uid: Int) {
            channel.playlist.deleteItem(uid)
        }

        override fun onMoveVideo(from: Int, after: Int) {
            channel.playlist.moveAfter(from, after)
        }

        override fun onMoveVideoToStart(uid: Int) {
            channel.playlist.moveToStart(uid)
        }

        override fun onPlaylistLock(locked: Boolean) {
            channel.playlist.locked.value = locked
        }

        override fun onConnect() {
            coroutineScope.launch { channel.reconnect() }
        }

        override fun onChannelJoin(channelName: String) {
            connectionStatus.currentChannel.value = channelName
            connectionStatus.disconnectReason.value = null

            channel.joinedChannel()
        }

        override fun onUserInitiatedDisconnect() {
            channel.disconnected()
        }

        override fun onDisconnect() {
            connectionStatus.disconnect()
            channel.disconnected()
        }

        override fun onConnectError() {
            channel.connectionError()
        }

        override fun onKick(reason: String) {
            connectionStatus.kicked.value = true
            connectionStatus.disconnect()
            channel.kicked(reason)
        }

        override fun onNewPoll(poll: Poll) {
            channel.newPoll(poll)
        }

        override fun onUpdateEmote(name: String, image: String) {
            channel.chat.updateEmote(ChatState.Emote(name = name, url = image))
        }

        override fun onRemoveEmote(name: String, image: String) {
            channel.chat.removeEmote(ChatState.Emote(name = name, url = image))
        }

        override fun updatePoll(poll: Poll) {
            channel.poll.updatePoll(poll)
        }

        override fun closePoll() {
            channel.poll.closeCurrent()
        }
    }

}

enum class MainActiveScreen {
    HOME,
    CHANNEL
}
