package org.snd.ui.videoplayer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class VideoPlayerState {
    internal val lengthMutable = MutableStateFlow(VideoLength())

    val time: MutableStateFlow<TimeState> = MutableStateFlow(TimeState.time(0))
    val isPlaying: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val length: StateFlow<VideoLength> = lengthMutable
    var mrl by mutableStateOf<String?>(null)

    fun seekTo(time: Long) {
        this.time.value = TimeState.time(time)
    }
    fun play() {
        isPlaying.value = true
    }
    fun pause() {
        isPlaying.value = false
    }
}

data class VideoLength(
    val length: Long = -1L
) {
    fun isKnown() = length != -1L
}

data class TimeState internal constructor(
    val time: Long,
    internal val updatedInternally: Boolean,
) {
    companion object {
        fun time(time: Long): TimeState {
            return TimeState(time, false)
        }

        internal fun internalTime(time: Long): TimeState {
            return TimeState(time, true)
        }
    }
}
