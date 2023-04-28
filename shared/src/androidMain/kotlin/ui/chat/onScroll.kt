package ui.chat

import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.debugInspectorInfo

actual fun Modifier.onScroll(onScroll: () -> Unit) = composed(inspectorInfo = debugInspectorInfo {
    name = "onScroll"
}) {
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y >= 1.0) onScroll()
                return Offset.Zero
            }
        }
    }

    this.then(Modifier.nestedScroll(nestedScrollConnection))
}