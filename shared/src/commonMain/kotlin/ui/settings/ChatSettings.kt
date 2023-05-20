package ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.Checkbox
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt


@Composable
fun ChatSettings(settings: SettingsState) {
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
            description = "Emotes Size",
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
                    timestampFormat = value.trim()
                    settings.timestampFormat = value
                } catch (e: IllegalArgumentException) {
                    //ignore
                }
            },
            label = { Text("Timestamp format") },
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = settings.showUserConnectionMessages,
                onCheckedChange = {
                    settings.showUserConnectionMessages = !settings.showUserConnectionMessages
                }
            )
            Text("Show User Connection Messages")
        }
    }
}
