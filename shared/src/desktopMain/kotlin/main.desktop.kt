import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.Dp
import cytube.SimpleCookieJar
import image.DesktopImageLoader
import kotlinx.coroutines.launch
import mu.KotlinLogging
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import ui.Channel
import ui.createModel

fun createState(): Channel {
    val httpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor { message -> KotlinLogging.logger { }.info { message } }
            .setLevel(HttpLoggingInterceptor.Level.BASIC))
        .cookieJar(SimpleCookieJar())
        .build()

    return createModel(httpClient, DesktopImageLoader(httpClient))
}

@Composable
fun MainView(
    state: Channel,
    windowHeight: Dp,
    windowWidth: Dp,
) {
    val coroutineScope = rememberCoroutineScope()
    coroutineScope.launch { state.init() }
    ui.MainView(state, windowHeight, windowWidth)
}

