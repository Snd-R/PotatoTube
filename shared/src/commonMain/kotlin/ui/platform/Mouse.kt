package ui.platform

import androidx.compose.ui.Modifier

expect fun Modifier.cursorForHorizontalResize(): Modifier

expect fun Modifier.cursorForMove(): Modifier

expect fun Modifier.cursorForHand(): Modifier
