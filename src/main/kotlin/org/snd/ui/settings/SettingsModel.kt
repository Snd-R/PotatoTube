package org.snd.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.LoadState
import org.snd.cytube.CytubeClient
import org.snd.settings.CytubeState
import org.snd.settings.Settings
import org.snd.settings.SettingsRepository

class SettingsModel(
    private val settingsRepository: SettingsRepository,
    private val cytube: CytubeClient,
    val cytubeState: CytubeState,
) {
    var currentTab by mutableStateOf(CurrentTab.CHAT)

    var fontSize by mutableStateOf(13.sp)
    var emoteSize by mutableStateOf(120.sp)
    var timestampFormat by mutableStateOf("")
    var historySize by mutableStateOf(0)

    var channel by mutableStateOf<String?>(null)

    var username by mutableStateOf<String?>(null)
    var password by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(false)

    suspend fun save() {
        settingsRepository.saveSettings(
            Settings(
                fontSize = fontSize.value,
                emoteSize = emoteSize.value,
                timestampFormat = timestampFormat,
                historySize = historySize,
                currentChannel = channel,
                accountName = username
            )
        )
    }

    fun logout() {
        cytube.logout()
        username?.let { settingsRepository.deletePassword(it) }
        this.username = null
        this.password = null
    }

    fun disconnect() {
        cytube.disconnectFromChannel()
        channel = null

    }

    fun changeChannel(channelName: String) {
        channel = channelName
        cytube.changeChannel(channelName)
    }

    fun persistCredentials(username: String, password: String) {
        this.username = username
        this.password = password
        settingsRepository.setPassword(username, password)
    }

    suspend fun login(username: String, password: String): LoadState<String> {
        return cytube.login(username, password)
    }


    enum class CurrentTab {
        CHAT,
        ACCOUNT,
        CHANNEL,
        LOADING
    }
}