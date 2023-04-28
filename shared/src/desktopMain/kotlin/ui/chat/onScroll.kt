package ui.chat

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType.Companion.Scroll
import androidx.compose.ui.input.pointer.onPointerEvent

@OptIn(ExperimentalComposeUiApi::class)
actual fun Modifier.onScroll(onScroll: () -> Unit) = this.then(
    Modifier.onPointerEvent(eventType = Scroll) { event ->
        if (event.changes.any { it.scrollDelta.y <= -1.0 })
            onScroll()
    }
)