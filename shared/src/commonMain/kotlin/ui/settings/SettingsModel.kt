package ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.sp
import cytube.CytubeClient
import player.PlayerType
import settings.Settings
import settings.SettingsRepository
import ui.ConnectionStatus

class SettingsModel(
    val connectionStatus: ConnectionStatus,
    val settingsRepository: SettingsRepository,
    private val cytube: CytubeClient,
) {
    var isActiveScreen by mutableStateOf(false)
    var currentTab by mutableStateOf(CurrentTab.CHAT)
    var fontSize by mutableStateOf(13.sp)
    var emoteSize by mutableStateOf(120.sp)
    var isTimestampsEnabled by mutableStateOf(false)
    var timestampFormat by mutableStateOf("")
    var historySize by mutableStateOf(0)
    var channel by mutableStateOf<String?>(null)
    var username by mutableStateOf<String?>(null)
    var syncThreshold by mutableStateOf(2000L)
    var isLoading by mutableStateOf(false)

    var playerType by mutableStateOf<PlayerType?>(null)

    suspend fun save() {
        settingsRepository.saveSettings(
            Settings(
                fontSize = fontSize.value,
                emoteSize = emoteSize.value,
                isTimestampsEnabled = isTimestampsEnabled,
                timestampFormat = timestampFormat,
                historySize = historySize,
                currentChannel = channel,
                accountName = username,
                player = playerType
            )
        )
    }

    suspend fun logout() {
        username?.let { settingsRepository.deletePassword(it) }
        username = null

        val channelName = channel
        if (channelName != null) {
            cytube.connect(channelName)
            connectionStatus.currentChannel = channelName
        }
    }

    fun disconnect() {
        channel = null
        cytube.disconnect()
    }

    suspend fun changeChannel(channelName: String) {
        channel = null
        connectionStatus.currentChannel = null
        cytube.connect(channelName)
        connectionStatus.currentChannel = channelName
        channel = channelName

        val username = username
        val password = username?.let {
            settingsRepository.loadPassword(it)
        }
        if (username != null && password != null)
            cytube.login(username, password)
    }

    fun persistCredentials(username: String, password: String) {
        this.username = username
        settingsRepository.setPassword(username, password)
    }

    suspend fun login(username: String, password: String): String {
        return cytube.login(username, password)
    }

    enum class CurrentTab {
        CHAT,
        ACCOUNT,
        CHANNEL,
        PLAYBACK
    }
}