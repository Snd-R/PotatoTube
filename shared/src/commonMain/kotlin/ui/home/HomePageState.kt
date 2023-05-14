package ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cytube.CytubeClient
import mu.KotlinLogging
import ui.ConnectionStatus
import ui.settings.SettingsState

private val logger = KotlinLogging.logger {}

class HomePageState(
    val settings: SettingsState,
    val connectionStatus: ConnectionStatus,
    private val cytubeClient: CytubeClient
) {
    var errorMessages = mutableStateListOf<String>()

    var activeScreen by mutableStateOf(HomePageActiveScreen.HOME)

    fun openHomeScreen() {
        this.activeScreen = HomePageActiveScreen.HOME
    }

    fun openSettingsScreen() {
        this.activeScreen = HomePageActiveScreen.SETTINGS
    }

    fun connect() {
        cytubeClient.connectToDefaultPartition()
    }

     suspend fun login() {
        val username = settings.username
        val password = username?.let { settings.settingsRepository.loadPassword(it) }
        if (username != null && password != null) {
            try {
                cytubeClient.login(username, password)
            } catch (e: Exception) {
                logger.error(e) { }
                settings.username = null
                errorMessages.add("Failed to login: ${e.message}")
            }
        }
    }

    fun disconnect() {
        cytubeClient.disconnect()
    }
}

enum class HomePageActiveScreen {
    HOME,
    SETTINGS
}
