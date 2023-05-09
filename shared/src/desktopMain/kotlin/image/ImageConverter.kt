package image

import com.twelvemonkeys.image.ResampleOp
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

object ImageConverter {

    fun scaleImage(image: ByteArray, height: Int?, width: Int?): Image {
        if (height == null && width == null) throw IllegalStateException("scale height and width cannot be null")

        val mediaType = ContentDetector.getMediaType(image)
        if (!ContentDetector.isSupportedMediaType(mediaType))
            throw IllegalStateException("Unsupported image format $mediaType")

        if (ContentDetector.isAnimated(mediaType))
            throw IllegalStateException("Scaling of animated images is not supported")

        val imageType = ContentDetector.toImageType(mediaType)
            ?: throw IllegalStateException("Can't detect image type before scaling")


        val bufferedImage = ImageIO.read(image.inputStream())
        val scaleTo = when {
            height != null && width != null -> scaleDimensions(bufferedImage, height, width)
            height != null -> scaleDimensionsByMaxHeight(bufferedImage, height)
            else -> scaleDimensionsByMaxWidth(bufferedImage, width!!)
        }
        val resampled = ResampleOp(scaleTo.width, scaleTo.height, ResampleOp.FILTER_LANCZOS)
            .filter(bufferedImage, null)

        val outputStream = ByteArrayOutputStream()
        ImageIO.write(resampled, imageType.imageIOFormat, outputStream)
        return Image(
            image = outputStream.toByteArray(),
            dimensions = Dimensions(
                height = resampled.height,
                width = resampled.width
            ),
        )
    }

    private fun scaleDimensions(from: BufferedImage, maxHeight: Int, maxWidth: Int): Dimensions {
        val bestRatio = (maxWidth / from.width.toFloat()).coerceAtMost(maxHeight / from.height.toFloat())
        return Dimensions(
            width = (from.width * bestRatio).toInt(),
            height = (from.height * bestRatio).toInt()
        )
    }

    private fun scaleDimensionsByMaxHeight(from: BufferedImage, maxHeight: Int): Dimensions {
        val ratio = maxHeight.toDouble() / from.height
        return Dimensions(
            width = (from.width * ratio).toInt(),
            height = (from.height * ratio).toInt()
        )
    }

    private fun scaleDimensionsByMaxWidth(from: BufferedImage, maxWidth: Int): Dimensions {
        val ratio = maxWidth.toDouble() / from.width
        return Dimensions(
            width = (from.width * ratio).toInt(),
            height = (from.height * ratio).toInt()
        )
    }
}
