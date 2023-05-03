import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.Dimension

val state = createState()

fun main() = application {
    val windowState = rememberWindowState(
        placement = WindowPlacement.Maximized,
        size = DpSize(1280.dp, 720.dp),
    )

    Window(
        title = "PotatoTube",
        onCloseRequest = ::exitApplication,
        state = windowState,
        icon = BitmapPainter(useResource("ic_launcher.png", ::loadImageBitmap)),
    ) {
        window.minimumSize = Dimension(500, 500)
        val horizontalInsets = window.insets.top + window.insets.bottom
        MainView(
            state = state,
            windowHeight = windowState.size.height - horizontalInsets.dp,
            windowWidth = windowState.size.width,
        )
    }
}
