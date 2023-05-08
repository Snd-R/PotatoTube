package player

import kotlinx.serialization.Serializable

@Serializable(with = PlayerTypeSerializer::class)
actual class PlayerType actual constructor(type: String) {
    actual val type: String

    init {
        this.type = type
    }
}
