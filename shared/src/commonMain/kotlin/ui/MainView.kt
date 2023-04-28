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
import ui.common.CustomTheme
import ui.settings.SettingsModel

@Composable
fun MainView(model: Channel, windowHeight: Dp, windowWidth: Dp) {
    CustomTheme(windowHeight, windowWidth) {
        Surface {
            CytubeMainView(model)
        }
    }
}

fun createModel(httpClient: OkHttpClient, imageLoader: ImageLoader): Channel {
    val moshi = Moshi.Builder().build()
    val cytubeClient = CytubeClient(httpClient, moshi)
    val settingsRepository = getSettingsRepository()
    val connectionStatus = ConnectionStatus()

    val settings = runBlocking {
        val config = settingsRepository.loadSettings()
        SettingsModel(connectionStatus, settingsRepository, cytubeClient).apply {
            fontSize = config.fontSize.sp
            timestampFormat = config.timestampFormat
            emoteSize = config.emoteSize.sp
            historySize = config.historySize
            username = config.accountName
            channel = config.currentChannel
        }
    }

    return Channel(
        connectionStatus = connectionStatus,
        settings = settings,
        cytube = cytubeClient,
        imageLoader = imageLoader
    )
}
