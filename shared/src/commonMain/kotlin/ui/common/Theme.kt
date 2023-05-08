package ui.common

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CustomTheme(
    windowHeight: Dp,
    windowWidth: Dp,
    content: @Composable () -> Unit,
) {
    val windowSize = WindowSize.basedOnWidth(windowWidth)

    val orientation = when {
        windowHeight.value > windowWidth.value -> Orientation.PORTRAIT
        else -> Orientation.LANDSCAPE
    }

    CompositionLocalProvider(
        LocalCustomColors provides AppTheme.CustomColors(),
        LocalWindowHeight provides windowHeight,
        LocalWindowSize provides windowSize,
        LocalOrientation provides orientation
    ) {
        MaterialTheme(colors = AppTheme.colors.material) {
            content()
        }
    }
}

val LocalCustomColors = staticCompositionLocalOf { AppTheme.CustomColors() }
val LocalWindowHeight = compositionLocalOf { 0.dp }
val LocalWindowSize = compositionLocalOf { WindowSize.COMPACT }
val LocalOrientation = compositionLocalOf { Orientation.LANDSCAPE }

object AppTheme {
    val colors: CustomColors
        @ReadOnlyComposable
        @Composable
        get() = LocalCustomColors.current

    class CustomColors(
        val backgroundDark: Color = Color(0xFF2B2B2B),
        val backgroundDarker: Color = Color(0XFF313335),
        val backgroundMedium: Color = Color(0xFF3C3F41),
        val backgroundLight: Color = Color(0xFF4E5254),
        val backgroundLighter: Color = Color(0xFF717476),
        val highlight: Color = Color(0XFFf2efd0),
        val buttonActive: Color = Color(0XFF34EB8F),
        val announcementMessageColor: Color = Color(0XFFF7A52A),
        val systemMessageColor: Color = Color(0XFF9C9C9C),
        val green: Color = Color(0XFF4FD172),

        val guest: Color = Color(0XFF888888),
        val regularUser: Color = Color(0XFFC8C8C8),
        val moderator: Color = Color(0XFF00AA00),
        val channelAdmin: Color = Color(0XFFFF9900),
        val siteAdmin: Color = Color(0XFFCC0000),

        val material: androidx.compose.material.Colors = darkColors(
            background = backgroundDark,
            surface = backgroundMedium,
            primary = Color.White
        ),
    )
}

enum class Orientation {
    LANDSCAPE,
    PORTRAIT;
}

enum class WindowSize {
    COMPACT,
    MEDIUM,
    EXPANDED,
    FULL;

    companion object {
        fun basedOnWidth(windowWidth: Dp): WindowSize {
            return when {
                windowWidth < 600.dp -> COMPACT
                windowWidth < 840.dp -> MEDIUM
                windowWidth < 1600.dp -> EXPANDED
                else -> FULL
            }
        }
    }
}