package ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import ui.common.OverlayDialogValue.Hidden
import ui.common.OverlayDialogValue.Shown

@Composable
fun OverlayDialog(
    overlayContent: @Composable ColumnScope.() -> Unit,
    overlayState: OverlayDialogState = remember { OverlayDialogState(Hidden) },
    modifier: Modifier = Modifier,
    scrimColor: Color = MaterialTheme.colors.onSurface.copy(alpha = 0.32f),
    content: @Composable () -> Unit
) {

    BoxWithConstraints(modifier) {
        Box(Modifier.fillMaxSize()) {
            content()

            if (overlayState.isOpen) {
                Scrim(color = scrimColor)
            }
        }

        if (overlayState.isOpen) {
            Surface(
                Modifier
                    .align(Alignment.Center)
                    .widthIn(max = MaxSheetWidth)
                    .fillMaxWidth(),
            ) {
                Column(content = overlayContent)
            }
        }
    }

}

@Composable
private fun Scrim(color: Color) {
    if (color.isSpecified) {
        Canvas(
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {}
        ) {
            drawRect(color = color)
        }
    }
}

class OverlayDialogState(initialValue: OverlayDialogValue) {
    private var currentValue: OverlayDialogValue by mutableStateOf(initialValue)

    fun open() {
        currentValue = Shown
    }

    val isOpen: Boolean
        get() = currentValue == Shown


    fun close() {
        currentValue = Hidden

    }
}

enum class OverlayDialogValue {
    Hidden,
    Shown
}

private val MaxSheetWidth = 1200.dp
