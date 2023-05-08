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
import ui.channel.ChannelState
import ui.channel.ConnectionStatus
import ui.channel.CytubeMainView
import ui.common.CustomTheme
import ui.settings.SettingsState

@Composable
fun MainView(model: ChannelState, windowHeight: Dp, windowWidth: Dp) {
    CustomTheme(windowHeight, windowWidth) {
        Surface {
            CytubeMainView(model)
        }
    }
}

fun createModel(httpClient: OkHttpClient, imageLoader: ImageLoader): ChannelState {
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
            channel = config.currentChannel
            playerType = config.player
        }
    }

    return ChannelState(
        connectionStatus = connectionStatus,
        settings = settings,
        cytube = cytubeClient,
        imageLoader = imageLoader
    )
}
