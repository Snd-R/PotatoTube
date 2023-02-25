package org.snd

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import com.squareup.moshi.Moshi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.snd.cytube.CytubeClient
import org.snd.cytube.SimpleCookieJar
import org.snd.image.DiscCache
import org.snd.image.ImageLoaderImpl
import org.snd.settings.SettingsRepository
import org.snd.ui.Channel
import org.snd.ui.ConnectionStatus
import org.snd.ui.MainView
import org.snd.ui.settings.SettingsModel

fun main() = singleWindowApplication(
    title = "PotatoTube",
    state = WindowState(width = 1280.dp, height = 720.dp),
    icon = BitmapPainter(useResource("ic_launcher.png", ::loadImageBitmap)),
) {
    val model = createModel()
    val coroutineScope = rememberCoroutineScope()
    coroutineScope.launch { model.init() }
    MainView(model)
}

@Composable
private fun createModel(): Channel {
    val httpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor { message -> KotlinLogging.logger { }.info { message } }
            .setLevel(HttpLoggingInterceptor.Level.BASIC))
        .cookieJar(SimpleCookieJar())
        .build()
    val discCache = DiscCache().apply { initialize() }
    val imageLoader = ImageLoaderImpl(httpClient, discCache)

    val moshi = Moshi.Builder().build()
    val cytubeClient = CytubeClient(httpClient, moshi)
    val settingsRepository = SettingsRepository()
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
