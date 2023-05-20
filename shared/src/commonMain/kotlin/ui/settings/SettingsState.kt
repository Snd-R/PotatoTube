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
import ui.MainActiveScreen

class SettingsState(
    val connectionStatus: ConnectionStatus,
    val settingsRepository: SettingsRepository,
    private val cytube: CytubeClient,
) {
    var activeScreen by mutableStateOf(MainActiveScreen.HOME)
    var channel by mutableStateOf<String?>(null)

    var currentTab by mutableStateOf(CurrentTab.CHAT)
    var fontSize by mutableStateOf(13.sp)
    var emoteSize by mutableStateOf(120.sp)
    var timestampFormat by mutableStateOf("")
    var historySize by mutableStateOf(0)
    var username by mutableStateOf<String?>(null)
    var allowGuestLogin by mutableStateOf(false)
    var syncThreshold by mutableStateOf(2000L)
    var isLoading by mutableStateOf(false)

    var playerType by mutableStateOf<PlayerType?>(null)
    var favoriteChannels = mutableStateOf<List<String>>(emptyList())

    suspend fun save() {
        settingsRepository.saveSettings(
            Settings(
                fontSize = fontSize.value,
                emoteSize = emoteSize.value,
                timestampFormat = timestampFormat,
                historySize = historySize,
                accountName = username,
                player = playerType,
                favoriteChannels = favoriteChannels.value
            )
        )
    }

    fun logout() {
        username?.let { settingsRepository.deletePassword(it) }
        username = null
    }

    fun disconnect() {
        channel = null
        cytube.disconnect()
        activeScreen = MainActiveScreen.HOME
    }

    fun changeChannel(channelName: String) {
        channel = channelName
        activeScreen = MainActiveScreen.CHANNEL
    }

    fun persistCredentials(username: String, password: String) {
        this.username = username
        settingsRepository.setPassword(username, password)
    }

    fun addFavoriteChannel(channelName: String) {
        favoriteChannels.value = favoriteChannels.value.plus(channelName)
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