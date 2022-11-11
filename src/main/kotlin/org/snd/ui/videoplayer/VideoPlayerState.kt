package org.snd.ui.videoplayer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.snd.ui.settings.SettingsModel
import java.time.Duration
import kotlin.math.absoluteValue

class VideoPlayerState(
    private val settingsModel: SettingsModel
) {

    var time = TimeState()
    var isPlaying by mutableStateOf(true)
    var length by mutableStateOf(-1L)
    var volume by mutableStateOf(50)
    var isMuted by mutableStateOf(false)

    var mrl by mutableStateOf<String?>(null)

    fun seekTo(time: Long) {
        this.time.update(time)
    }

    fun play() {
        isPlaying = true
    }

    fun pause() {
        isPlaying = false
    }

    fun toggleMute() {
        isMuted = !isMuted
    }

    fun sync(newTime: Long, paused: Boolean) {
        val timeDiff = (newTime - time.value).absoluteValue
        if (timeDiff > settingsModel.syncThreshold)
            time.update(newTime)

        if (!paused && !isPlaying) play()
        else if (paused && isPlaying) pause()
    }

    fun lengthString(): String {
        return durationString(length)
    }

    fun currentTimeString(): String {
        return durationString(time.value)
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
    var value by mutableStateOf(0L)
        private set

    // track external updates that are not from video player natural playback
    var updatedExternallyToggle by mutableStateOf(false)
        private set

    fun update(time: Long) {
        this.value = time
        this.updatedExternallyToggle = !this.updatedExternallyToggle
    }

    fun updateInternally(time: Long) {
        this.value = time
    }
}
