package image


enum class ImageType(val mediaType: String, val imageIOFormat: String) {
    PNG("image/png", "png"),
    JPEG("image/jpeg", "jpeg"),
}
