package ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import player.Player
import player.PlayerDiscovery
import player.PlayerType

@Composable
actual fun PlayerTypeSettings(model: SettingsModel) {
    Column {
        Text("Player Type")
        PlayerTypeOption(model, Player.MPV_EMBEDDED)
        PlayerTypeOption(model, Player.VLC_EMBEDDED)
        PlayerTypeOption(model, Player.VLC_DIRECT)
    }
}

@Composable
fun PlayerTypeOption(model: SettingsModel, type: Player) {
    val currentType = model.playerType?.player
    val (playerName, isAvailable) = when (type) {
        Player.VLC_EMBEDDED -> "VLC Embedded" to PlayerDiscovery.isVlcEmbeddedAvailable
        Player.VLC_DIRECT -> "VLC Direct (High CPU usage)" to PlayerDiscovery.isVlcAvailable
        Player.MPV_EMBEDDED -> "MPV Embedded" to PlayerDiscovery.isMpvEmbeddedAvailable
    }

    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable(enabled = isAvailable) { model.playerType = PlayerType(type) }
    ) {
        RadioButton(
            selected = currentType == type,
            enabled = isAvailable,
            onClick = { model.playerType = PlayerType(type) },
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.size(5.dp))
        Text(playerName)
        if (!isAvailable)
            Text(" (Unavailable)")

    }
}
