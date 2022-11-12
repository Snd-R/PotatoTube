package org.snd.cytube

import org.snd.ui.Channel
import org.snd.ui.chat.Chat
import org.snd.ui.playlist.PlaylistItem

class CytubeEventHandler(
    private val channel: Channel,
) {

    fun onChatMessage(message: Chat.Message) {
        channel.chat.addMessage(message)
    }

    fun onLoginSuccess(name: String, isGuest: Boolean = false) {
        channel.userStatus.currentUser = name
        channel.userStatus.isGuest = isGuest
    }

    fun onLoginFailure() {

    }

    fun onEmoteList(emotes: List<Chat.Emote>) {
        channel.chat.setEmotes(emotes)
    }

    fun onUserList(users: List<Chat.User>) {
        channel.chat.users.setUsers(users)
    }

    fun onUserCount(count: Int) {
        channel.chat.users.userCount(count)
    }

    fun onSetAfk(username: String, afk: Boolean) {
        channel.chat.users.setAfk(username, afk)
    }

    fun onAddUser(user: Chat.User) {
        channel.chat.users.addUser(user)
    }

    fun onUserLeave(username: String) {
        channel.chat.users.removeUser(username)
    }

    fun onChangeMedia(url: String) {
        channel.player.mrl = url
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
        channel.connected()
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
}