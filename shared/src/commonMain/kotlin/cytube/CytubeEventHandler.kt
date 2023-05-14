package cytube

import ui.chat.ChatState
import ui.playlist.PlaylistItem
import ui.poll.Poll

interface CytubeEventHandler {

    fun onChatMessage(message: ChatState.Message.UserMessage) {}

    fun onLoginSuccess(name: String, isGuest: Boolean = false) {}

    fun onEmoteList(emotes: List<ChatState.Emote>) {}

    fun onUserList(users: List<ChatState.User>) {}

    fun onUserCount(count: Int) {}

    fun onSetAfk(username: String, afk: Boolean) {}

    fun onAddUser(user: ChatState.User) {}

    fun onUserLeave(username: String) {}

    fun onChangeMedia(url: String) {}

    fun onMediaUpdate(timeMillis: Long, paused: Boolean) {}

    fun onQueue(item: PlaylistItem, position: String) {}

    fun onPlaylist(items: List<PlaylistItem>) {}

    fun onPlaylistMeta(rawTime: Long, count: Int, time: String) {}

    fun onDeletePlaylistItem(uid: Int) {}

    fun onMoveVideo(from: Int, after: Int) {}

    fun onMoveVideoToStart(uid: Int) {}

    fun onPlaylistLock(locked: Boolean) {}

    fun onConnect() {}

    fun onChannelJoin(channelName: String) {}

    fun onUserInitiatedDisconnect() {}

    fun onDisconnect() {}

    fun onConnectError() {}

    fun onKick(reason: String) {}

    fun onNewPoll(poll: Poll) {}

    fun onUpdateEmote(name: String, image: String) {}

    fun onRemoveEmote(name: String, image: String) {}

    fun updatePoll(poll: Poll) {}

    fun closePoll() {}
}