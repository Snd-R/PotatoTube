package ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp

@Composable
fun PlaybackSettings(model: SettingsModel) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Playback Settings")
        Divider()
        SyncThresholdSettings(model)
        PlayerTypeSettings(model)
    }
}

@Composable
fun SyncThresholdSettings(model: SettingsModel) {
    var threshold by remember { mutableStateOf((model.syncThreshold / 1000).toString()) }
    TextField(
        value = threshold,
        label = { Text(text = "Sync threshold (seconds)") },
        onValueChange = { value ->
            if (value.length < 4) {
                threshold = value.filter { it.isDigit() }
                if (threshold.isNotBlank())
                    model.syncThreshold = threshold.toLong() * 1000
            }
        }
    )
}

@Composable
expect fun PlayerTypeSettings(model: SettingsModel)
