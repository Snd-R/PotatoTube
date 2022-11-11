package org.snd.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt


@Composable
fun ChatSettings(settings: SettingsModel) {
    Column {
        Text("Chat settings")
        Spacer(Modifier.size(5.dp))
        Divider()
        Spacer(Modifier.size(15.dp))
        SliderOption(
            description = "Font Size",
            initialValue = settings.fontSize.value,
            valueRange = 6f..20f,
            steps = 13
        ) { settings.fontSize = it.roundToInt().sp }

        SliderOption(
            description = "Emote Size",
            initialValue = settings.emoteSize.value,
            valueRange = 50f..300f,
            steps = 0
        ) { settings.emoteSize = it.roundToInt().sp }
        var historySize by remember { mutableStateOf(settings.historySize.toString()) }
        TextField(
            historySize,
            onValueChange = { value ->
                if (value.length <= 5) {
                    historySize = value.filter { it.isDigit() }
                    if (historySize.isNotBlank())
                        settings.historySize = historySize.toInt()
                }
            },
            label = { Text("Message History size") },
        )
        Spacer(Modifier.size(15.dp))

        var timestampFormat by remember { mutableStateOf(settings.timestampFormat) }
        TextField(
            timestampFormat,
            onValueChange = { value ->
                try {
                    DateTimeFormatter.ofPattern(value)
                    timestampFormat = value
                    settings.timestampFormat = value
                } catch (e: IllegalArgumentException) {
                    //ignore
                }
            },
            label = { Text("Timestamp format") },
        )
    }
}
