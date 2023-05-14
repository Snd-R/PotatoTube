package ui

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.squareup.moshi.Moshi
import cytube.CytubeClient
import image.ImageLoader
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import settings.getSettingsRepository
import ui.MainActiveScreen.CHANNEL
import ui.MainActiveScreen.HOME
import ui.channel.ChannelState
import ui.channel.ChannelView
import ui.common.CustomTheme
import ui.home.HomePageState
import ui.home.HomePageView
import ui.settings.SettingsState

@Composable
fun MainView(state: AppState, windowHeight: Dp, windowWidth: Dp) {
    CustomTheme(windowHeight, windowWidth) {
        Surface {
            when (state.settings.activeScreen) {
                HOME -> HomePageView(state.homePage)
                CHANNEL -> ChannelView(state.channel)
            }
        }
    }
}

fun createModel(httpClient: OkHttpClient, imageLoader: ImageLoader): AppState {
    val moshi = Moshi.Builder().build()

    val cytubeClient = CytubeClient(httpClient, moshi)
    val settingsRepository = getSettingsRepository()

    val connectionStatus = ConnectionStatus()

    val settings = runBlocking {
        val config = settingsRepository.loadSettings()
        SettingsState(
            connectionStatus,
            settingsRepository,
            cytubeClient
        ).apply {
            fontSize = config.fontSize.sp
            timestampFormat = config.timestampFormat
            emoteSize = config.emoteSize.sp
            historySize = config.historySize
            username = config.accountName
            playerType = config.player
            favoriteChannels.value = config.favoriteChannels

        }
    }

    val channel = ChannelState(
        connectionStatus = connectionStatus,
        settings = settings,
        cytube = cytubeClient,
        imageLoader = imageLoader
    )
    val homePage = HomePageState(settings, connectionStatus, cytubeClient)

    return AppState(channel, homePage, settings, connectionStatus, cytubeClient)
}
