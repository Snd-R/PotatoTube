package settings

import kotlinx.serialization.Serializable
import player.PlayerType

@Serializable
data class Settings(
    val fontSize: Float = 13f,
    val emoteSize: Float = 120f,
    val timestampFormat: String = "HH:mm:ss",
    val historySize: Int = 1000,
    val accountName: String? = null,
    val showUserConnectionMessages: Boolean = true,

    val player: PlayerType? = null,
    val favoriteChannels: List<String> = emptyList()
)
