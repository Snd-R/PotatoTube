package ui.platform

import androidx.compose.runtime.Composable

@Composable
expect fun Tooltip(
    tooltip: @Composable () -> Unit,
    delayMillis: Int,
    content: @Composable () -> Unit
)
