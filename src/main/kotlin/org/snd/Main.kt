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
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.snd.cytube.CytubeClient
import org.snd.cytube.SimpleCookieJar
import org.snd.image.DiscCache
import org.snd.image.ImageLoaderImpl
import org.snd.settings.SettingsRepository
import org.snd.ui.Channel
import org.snd.ui.MainView
import org.snd.ui.UserStatus
import org.snd.ui.settings.SettingsModel

fun main() = singleWindowApplication(
    title = "PotatoTube",
    state = WindowState(width = 1280.dp, height = 720.dp),
    icon = BitmapPainter(useResource("ic_launcher.png", ::loadImageBitmap)),
) {
    val model = createModel()
    val coroutineScope = rememberCoroutineScope()
    coroutineScope.launch {
        model.connect()
    }
    MainView(model)
}

@Composable
private fun createModel(): Channel {
    val httpClient = OkHttpClient.Builder()
//        .addInterceptor(HttpLoggingInterceptor { message -> KotlinLogging.logger { message } }
//            .setLevel(HttpLoggingInterceptor.Level.BODY))
        .cookieJar(SimpleCookieJar())
        .build()
    val discCache = DiscCache()
    discCache.initialize()
    val imageLoader = ImageLoaderImpl(httpClient, discCache)

    val cytubeClient = CytubeClient(httpClient)
    val settingsRepository = SettingsRepository()
    val userStatus = UserStatus()

    val settings = runBlocking {
        val config = settingsRepository.loadSettings()
        val userPassword = config.accountName?.let { settingsRepository.loadPassword(it) }
        SettingsModel(userStatus, settingsRepository, cytubeClient).apply {
            fontSize = config.fontSize.sp
            timestampFormat = config.timestampFormat
            emoteSize = config.emoteSize.sp
            historySize = config.historySize
            username = config.accountName
            password = userPassword
            channel = config.currentChannel
        }
    }

    return Channel(
        userStatus = userStatus,
        settings = settings,
        cytube = cytubeClient,
        imageLoader = imageLoader
    )
}
