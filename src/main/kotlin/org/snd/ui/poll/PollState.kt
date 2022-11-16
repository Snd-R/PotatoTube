package org.snd.ui.poll

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.snd.cytube.CytubeClient
import java.time.Instant

class PollState(
    private val cytube: CytubeClient,
) {
    var currentPoll by mutableStateOf(false)

    var title by mutableStateOf("")
    var timestamp by mutableStateOf(Instant.now())
    var totalCount by mutableStateOf(0)
    val options = mutableStateListOf<PollOption>()
    var chosenOption by mutableStateOf<Int?>(null)

    var closed by mutableStateOf(false)
    var showChatPoll by mutableStateOf(true)

    fun startNewPoll(poll: Poll) {
        options.clear()
        options.addAll(poll.options)
        totalCount = poll.totalCount
        title = poll.title
        timestamp = poll.timestamp
        currentPoll = true
        closed = false
        showChatPoll = true
        chosenOption = null
    }

    fun vote(option: PollOption) {
        cytube.pollVote(option.index)
        chosenOption = option.index
    }

    fun updatePoll(poll: Poll) {
        options.clear()
        options.addAll(poll.options)
        totalCount = poll.totalCount
    }

    fun closeCurrent() {
        closed = true
    }

    fun hideChatPoll() {
        showChatPoll = false
    }
}

data class PollOption(
    val name: String,
    val count: Int,
    val index: Int,
)

data class Poll(
    val title: String,
    val totalCount: Int,
    val options: Collection<PollOption>,
    val timestamp: Instant,
    val initiator: String
)