package ui.playlist

import cytube.CytubeClient
import cytube.YOUTUBE_PREFIX
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class PlaylistState(val cytube: CytubeClient) {
    var rawTime = MutableStateFlow(0L)
    var count = MutableStateFlow(0)
    var time = MutableStateFlow("")
    var locked = MutableStateFlow(false)

    val items = MutableStateFlow(emptyList<PlaylistItem>())

    fun setPlaylist(newItems: List<PlaylistItem>) {
        items.value = newItems
    }

    fun addFirst(item: PlaylistItem) {
        items.update { listOf(item) + it }
    }

    fun addItem(item: PlaylistItem, after: Int) {
        items.update { oldItems ->
            val newItems = ArrayList<PlaylistItem>(oldItems)
            val insertIndex = newItems.indexOfFirst { it.uid == after } + 1
            newItems.add(insertIndex, item)
            newItems
        }
    }

    fun deleteItem(uid: Int) {
        items.update { items -> items.filter { it.uid != uid } }
    }

    fun moveAfter(from: Int, after: Int) {
        items.update { oldItems ->
            val newItems = ArrayList<PlaylistItem>(oldItems)
            val fromIndex = newItems.indexOfFirst { it.uid == from }
            val item = newItems[fromIndex]
            newItems.removeAt(fromIndex)
            val toIndex = newItems.indexOfFirst { it.uid == after }
            newItems.add(toIndex + 1, item)

            newItems
        }

    }

    fun moveToStart(uid: Int) {
        items.update { oldItems ->
            val newItems = ArrayList<PlaylistItem>(oldItems)

            val itemIndex = newItems.indexOfFirst { it.uid == uid }
            val item = newItems[itemIndex]
            newItems.removeAt(itemIndex)
            newItems.add(0, item)

            newItems
        }
    }

    fun reset() {
        items.value = emptyList()
        rawTime.value = 0
        count.value = 0
        time.value = ""
    }
}

data class PlaylistItem(
    val uid: Int,
    val temp: Boolean,
    val queueBy: String,
    val media: MediaItem,
)

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