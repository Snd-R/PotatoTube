package settings

import kotlinx.serialization.Serializable
import player.PlayerType

@Serializable
data class Settings(
    val fontSize: Float = 13f,
    val emoteSize: Float = 120f,
    val isTimestampsEnabled: Boolean = false,
    val timestampFormat: String = "HH:mm:ss",
    val historySize: Int = 1000,
    val currentChannel: String? = null,
    val accountName: String? = null,

    val player: PlayerType? = null
)
