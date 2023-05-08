package player

import kotlinx.serialization.Serializable

enum class Player {
    VLC_EMBEDDED,
    VLC_DIRECT,
    MPV_EMBEDDED
}

@Serializable(with = PlayerTypeSerializer::class)
actual class PlayerType actual constructor(type: String) {
    actual val type: String
    val player: Player

    constructor(player: Player) : this(player.toString())

    init {
        this.type = type
        this.player = Player.valueOf(type)
    }
}
