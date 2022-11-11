package org.snd.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp

@Composable
fun PlaybackSettings(model: SettingsModel) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row {
            Column {
                Text("Playback Settings")
            }
        }


        Divider()
        var threshold by remember { mutableStateOf((model.syncThreshold / 1000).toString()) }
        TextField(
            value = threshold,
            label = { Text(text = "Synch threshold (seconds)") },
            onValueChange = { value ->
                if (value.length < 4) {
                    threshold = value.filter { it.isDigit() }
                    if (threshold.isNotBlank())
                        model.syncThreshold = threshold.toLong() * 1000

                }
            }
        )
    }
}