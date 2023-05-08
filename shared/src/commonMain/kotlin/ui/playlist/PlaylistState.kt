package ui.playlist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cytube.CytubeClient
import cytube.YOUTUBE_PREFIX

class PlaylistState(val cytube: CytubeClient) {
    var rawTime by mutableStateOf(0L)
    var count by mutableStateOf(0)
    var time by mutableStateOf("")
    var locked by mutableStateOf(false)

    val items = mutableStateListOf<PlaylistItem>()

    fun setPlaylist(newItems: List<PlaylistItem>) {
        items.clear()
        items.addAll(newItems)
    }

    fun addFirst(item: PlaylistItem) {
        items.add(0, item)
    }

    fun addItem(item: PlaylistItem, after: Int) {
        val insertIndex = items.indexOfFirst { it.uid == after } + 1
        items.add(insertIndex, item)
    }

    fun deleteItem(uid: Int) {
        items.removeIf { it.uid == uid }
    }

    fun moveAfter(from: Int, after: Int) {
        val fromIndex = items.indexOfFirst { it.uid == from }
        val item = items[fromIndex]

        items.removeAt(fromIndex)
        val toIndex = items.indexOfFirst { it.uid == after }
        items.add(toIndex + 1, item)
    }

    fun moveToStart(uid: Int) {
        val itemIndex = items.indexOfFirst { it.uid == uid }
        val item = items[itemIndex]
        items.removeAt(itemIndex)
        items.add(0, item)
    }

    fun reset() {
        items.clear()
        rawTime = 0
        count = 0
        time = ""
    }
}

data class PlaylistItem(
    val uid: Int,
    val temp: Boolean,
    val queueBy: String,
    val media: MediaItem,
) {
}

data class MediaItem(
    val duration: String,
    val seconds: Long,
    val id: String,
    val title: String,
    val type: String
) {
    fun getLink(): String {
        return if (type == "yt") YOUTUBE_PREFIX + id
        else id
    }
}