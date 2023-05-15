package ui.videoplayer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import ui.settings.SettingsState
import java.time.Duration
import kotlin.math.absoluteValue

class VideoPlayerState(
    private val settingsModel: SettingsState
) {
    var timeState = TimeState()
    var isPlaying = MutableStateFlow(true)
    var length = MutableStateFlow(-1L)
    var volume = MutableStateFlow(50)
    var isMuted = MutableStateFlow(false)
    var isBuffering = MutableStateFlow(false)

    var mrl = MutableStateFlow<String?>(null)

    var isInTheaterMode by mutableStateOf(false)

    fun seekTo(time: Long) {
        this.timeState.update(time)
    }

    fun play() {
        isPlaying.value = true
    }

    fun pause() {
        isPlaying.value = false
    }

    fun toggleMute() {
        isMuted.update { !it }
    }

    fun unmute() {
        isMuted.value = false
    }

    fun mute() {
        isMuted.value = true
    }

    fun setLength(length: Long) {
        this.length.value = length
    }

    fun setMrl(mrl: String?) {
        this.mrl.value = mrl
    }

    fun setBuffering(buffering: Boolean) {
        this.isBuffering.value = buffering
    }

    fun setVolume(volume: Int) {
        this.volume.value = volume
    }

    fun sync(newTime: Long, paused: Boolean) {
        val timeDiff = (newTime - timeState.time.value).absoluteValue

        if (timeDiff > settingsModel.syncThreshold)
            timeState.update(newTime)

        val isPlaying = this.isPlaying.value
        if (!paused && !isPlaying) play()
        else if (paused && isPlaying) pause()
    }

    fun lengthString(): String {
        return durationString(length.value)
    }

    fun currentTimeString(): String {
        return durationString(timeState.time.value)
    }

    fun toggleTheaterMode() {
        isInTheaterMode = !isInTheaterMode
    }

    private fun durationString(millis: Long): String {
        val duration = Duration.ofMillis(millis)
        val secondsPart = duration.toSecondsPart()
        val secondsPartString = if (secondsPart < 10) "0$secondsPart"
        else secondsPart.toString()
        return if (duration.toHoursPart() == 0)
            "${duration.toMinutesPart()}:$secondsPartString"
        else "${duration.toHoursPart()}:${duration.toMinutesPart()}:$secondsPartString"
    }
}

class TimeState {
    var time = MutableStateFlow(0L)
        private set


    // track external updates that are not from video player natural playback
    var updatedExternallyToggle = MutableStateFlow(false)
        private set

    fun update(time: Long) {
        this.time.value = time
        this.updatedExternallyToggle.update { !it }
    }

    fun updateInternally(time: Long) {
        this.time.value = time
    }
}
