import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import cytube.SimpleCookieJar
import image.DesktopImageLoader
import mu.KotlinLogging
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import player.PlayerDiscovery
import ui.AppState
import ui.createModel

fun createState(): AppState {
    PlayerDiscovery.discover()

    val httpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor { message -> KotlinLogging.logger { }.info { message } }
            .setLevel(HttpLoggingInterceptor.Level.BASIC))
        .cookieJar(SimpleCookieJar())
        .build()

    return createModel(httpClient, DesktopImageLoader(httpClient))
}

@Composable
fun MainView(
    state: AppState,
    windowHeight: Dp,
    windowWidth: Dp,
) {
    ui.MainView(state, windowHeight, windowWidth)
}

