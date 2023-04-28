import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import coil.Coil
import coil.ImageLoader
import coil.decode.ImageDecoderDecoder
import cytube.SimpleCookieJar
import image.AndroidImageLoader
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

    return createModel(httpClient, AndroidImageLoader())
}

@Composable
fun MainView(
    state: Channel,
    windowHeight: Dp,
    windowWidth: Dp
) {
    val context = LocalContext.current
    val imageLoader = ImageLoader.Builder(context)
        .components { add(ImageDecoderDecoder.Factory()) }
        .build()
    Coil.setImageLoader(imageLoader)

    LaunchedEffect(Unit) { state.init() }
    ui.MainView(state, windowHeight, windowWidth)
}
