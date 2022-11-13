package org.snd.ui

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import org.snd.ui.common.AppTheme


@Composable
fun MainView(model: Channel) {
    MaterialTheme(colors = AppTheme.colors.material) {
        Surface {
            CytubeMainView(model)
        }
    }
}