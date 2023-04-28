package cytube

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SocketConfig(
    val servers: List<CytubeServerPartition>
)

@JsonClass(generateAdapter = true)
data class CytubeServerPartition(
    val url: String,
    val secure: Boolean
)