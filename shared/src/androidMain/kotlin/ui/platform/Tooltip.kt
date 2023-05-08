package ui.platform

import androidx.compose.runtime.Composable

@Composable
actual fun Tooltip(
    tooltip: @Composable () -> Unit,
    delayMillis: Int,
    content: @Composable () -> Unit
) {
    // No Tooltip for Android
    content()
}