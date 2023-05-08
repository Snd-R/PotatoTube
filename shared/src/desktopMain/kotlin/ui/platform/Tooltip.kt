package ui.platform

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.runtime.Composable

@OptIn(ExperimentalFoundationApi::class)
@Composable
actual fun Tooltip(
    tooltip: @Composable () -> Unit,
    delayMillis: Int,
    content: @Composable () -> Unit
) {
    TooltipArea(
        tooltip = tooltip,
        delayMillis = delayMillis
    ) {
        content()
    }
}