package cytube

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ui.channel.ChannelState
import ui.chat.ChatState
import ui.playlist.PlaylistItem
import ui.poll.Poll

class CytubeEventHandler(
    private val channel: ChannelState,
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    fun onChatMessage(message: ChatState.Message.UserMessage) {
        channel.chat.addUserMessage(message)
    }

    fun onLoginSuccess(name: String, isGuest: Boolean = false) {
        channel.connectionStatus.currentUser = name
        channel.connectionStatus.isGuest = isGuest
    }

    fun onEmoteList(emotes: List<ChatState.Emote>) {
        channel.chat.setEmotes(emotes)
    }

    fun onUserList(users: List<ChatState.User>) {
        channel.chat.users.setUsers(users)
    }

    fun onUserCount(count: Int) {
        channel.chat.users.userCount(count)
    }

    fun onSetAfk(username: String, afk: Boolean) {
        channel.chat.users.setAfk(username, afk)
    }

    fun onAddUser(user: ChatState.User) {
        channel.chat.users.addUser(user)
    }

    fun onUserLeave(username: String) {
        channel.chat.users.removeUser(username)
    }

    fun onChangeMedia(url: String) {
        channel.player.setMrl(url)
    }

    fun onMediaUpdate(timeMillis: Long, paused: Boolean) {
        channel.player.sync(timeMillis, paused)
    }

    fun onQueue(item: PlaylistItem, position: String) {
        if (position == "prepend")
            channel.playlist.addFirst(item)
        else channel.playlist.addItem(item, position.toInt())
    }

    fun onPlaylist(items: List<PlaylistItem>) {
        channel.playlist.setPlaylist(items)
    }

    fun onPlaylistMeta(rawTime: Long, count: Int, time: String) {
        channel.playlist.rawTime = rawTime
        channel.playlist.count = count
        channel.playlist.time = time
    }

    fun onDeletePlaylistItem(uid: Int) {
        channel.playlist.deleteItem(uid)
    }

    fun onMoveVideo(from: Int, after: Int) {
        channel.playlist.moveAfter(from, after)
    }

    fun onMoveVideoToStart(uid: Int) {
        channel.playlist.moveToStart(uid)
    }

    fun onPlaylistLock(locked: Boolean) {
        channel.playlist.locked = locked
    }

    fun onConnect() {
        coroutineScope.launch { channel.reconnect() }
    }

    fun onChannelJoin(channelName: String) {
        channel.joinedChannel(channelName)
    }

    fun onUserInitiatedDisconnect() {
        channel.connectionStatus.hasConnectedBefore = false
        channel.disconnected()
    }

    fun onDisconnect() {
        channel.disconnected()
    }

    fun onConnectError() {
        channel.connectionError()
    }

    fun onKick(reason: String) {
        channel.kicked(reason)
    }

    fun onNewPoll(poll: Poll) {
        channel.newPoll(poll)
    }

    fun onUpdateEmote(name: String, image: String) {
        channel.chat.updateEmote(ChatState.Emote(name = name, url = image))
    }

    fun onRemoveEmote(name: String, image: String) {
        channel.chat.removeEmote(ChatState.Emote(name = name, url = image))
    }

    fun updatePoll(poll: Poll) {
        channel.poll.updatePoll(poll)
    }

    fun closePoll() {
        channel.poll.closeCurrent()
    }
}