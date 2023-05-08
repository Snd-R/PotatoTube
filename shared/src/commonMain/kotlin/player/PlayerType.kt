package player

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = PlayerTypeSerializer::class)
expect class PlayerType(type: String) {
    val type: String
}

object PlayerTypeSerializer : KSerializer<PlayerType> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("player", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: PlayerType) {
        encoder.encodeString(value.type)
    }

    override fun deserialize(decoder: Decoder): PlayerType {
        val string = decoder.decodeString()
        return PlayerType(string)
    }
}
