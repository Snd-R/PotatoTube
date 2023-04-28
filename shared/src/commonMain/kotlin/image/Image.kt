package image

class Image(
    val image: ByteArray,
    val dimension: Dimension,
    val scaled: Boolean = false,
)

data class Dimension(
    val width: Int,
    val height: Int,
)
