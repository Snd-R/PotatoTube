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

    val dimens = when (windowSize) {
        WindowSize.COMPACT -> compactDimens
        WindowSize.MEDIUM -> mediumDimens
        WindowSize.EXPANDED -> expandedDimens
        WindowSize.FULL -> expandedDimens
    }

    val orientation = when {
        windowHeight.value > windowWidth.value -> Orientation.PORTRAIT
        else -> Orientation.LANDSCAPE
    }

    CompositionLocalProvider(
        LocalCustomColors provides AppTheme.CustomColors(),
        LocalWindowHeight provides windowHeight,
        LocalWindowSize provides windowSize,
        LocalCustomDimens provides dimens,
        LocalOrientation provides orientation
    ) {
        MaterialTheme(colors = AppTheme.colors.material) {
            content()
        }
    }
}

val LocalCustomDimens = staticCompositionLocalOf { compactDimens }
val LocalCustomColors = staticCompositionLocalOf { AppTheme.CustomColors() }
val LocalWindowHeight = compositionLocalOf { 0.dp }
val LocalWindowSize = compositionLocalOf { WindowSize.COMPACT }
val LocalOrientation = compositionLocalOf { Orientation.LANDSCAPE }

object AppTheme {
    val colors: CustomColors
        @ReadOnlyComposable
        @Composable
        get() = LocalCustomColors.current

    val dimens: CustomDimens
        @ReadOnlyComposable
        @Composable
        get() = LocalCustomDimens.current

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

class CustomDimens(
    val grid_0_25: Dp,
    val grid_0_5: Dp,
    val grid_1: Dp,
    val grid_1_5: Dp,
    val grid_2: Dp,
    val grid_2_5: Dp,
    val grid_3: Dp,
    val grid_3_5: Dp,
    val grid_4: Dp,
    val grid_4_5: Dp,
    val grid_5: Dp,
    val grid_5_5: Dp,
    val grid_6: Dp,
    val borders_thickness: Dp,
)

val compactDimens = CustomDimens(
    grid_0_25 = 1.5f.dp,
    grid_0_5 = 3.dp,
    grid_1 = 6.dp,
    grid_1_5 = 9.dp,
    grid_2 = 12.dp,
    grid_2_5 = 15.dp,
    grid_3 = 18.dp,
    grid_3_5 = 21.dp,
    grid_4 = 24.dp,
    grid_4_5 = 27.dp,
    grid_5 = 30.dp,
    grid_5_5 = 33.dp,
    grid_6 = 36.dp,
    borders_thickness = 1.dp,
)

val mediumDimens = CustomDimens(
    grid_0_25 = 2.dp,
    grid_0_5 = 4.dp,
    grid_1 = 8.dp,
    grid_1_5 = 12.dp,
    grid_2 = 16.dp,
    grid_2_5 = 20.dp,
    grid_3 = 24.dp,
    grid_3_5 = 28.dp,
    grid_4 = 32.dp,
    grid_4_5 = 36.dp,
    grid_5 = 40.dp,
    grid_5_5 = 44.dp,
    grid_6 = 48.dp,
    borders_thickness = 2.dp,
)

val expandedDimens = CustomDimens(
    grid_0_25 = 2.5f.dp,
    grid_0_5 = 5.dp,
    grid_1 = 10.dp,
    grid_1_5 = 15.dp,
    grid_2 = 20.dp,
    grid_2_5 = 25.dp,
    grid_3 = 30.dp,
    grid_3_5 = 35.dp,
    grid_4 = 40.dp,
    grid_4_5 = 45.dp,
    grid_5 = 50.dp,
    grid_5_5 = 55.dp,
    grid_6 = 60.dp,
    borders_thickness = 3.dp,
)