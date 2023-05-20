package ui.poll

import cytube.CytubeClient
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.Instant

class PollState(
    private val cytube: CytubeClient,
) {
    var currentPoll = MutableStateFlow(false)

    var title = MutableStateFlow("")
    private var timestamp = MutableStateFlow(Instant.now())
    var totalCount = MutableStateFlow(0)
    val options = MutableStateFlow(emptyList<PollOption>())
    var chosenOption = MutableStateFlow<Int?>(null)

    var closed = MutableStateFlow(false)
    var showChatPoll = MutableStateFlow(true)

    fun startNewPoll(poll: Poll) {
        options.value = poll.options
        totalCount.value = poll.totalCount
        title.value = poll.title
        timestamp.value = poll.timestamp
        currentPoll.value = true
        closed.value = false
        showChatPoll.value = true
        chosenOption.value = null
    }

    fun vote(option: PollOption) {
        cytube.pollVote(option.index)
        chosenOption.value = option.index
    }

    fun updatePoll(poll: Poll) {
        options.value = poll.options
        totalCount.value = poll.totalCount
    }

    fun closeCurrent() {
        closed.value = true
    }

    fun hideChatPoll() {
        showChatPoll.value = false
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
    val options: List<PollOption>,
    val timestamp: Instant,
    val initiator: String
)