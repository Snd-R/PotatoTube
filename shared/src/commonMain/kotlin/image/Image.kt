package image

class Image(
    val image: ByteArray,
    val dimensions: Dimensions,
)

data class Dimensions(
    val width: Int,
    val height: Int,
)
