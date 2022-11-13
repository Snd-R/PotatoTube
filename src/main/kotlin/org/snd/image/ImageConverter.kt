package org.snd.image

import com.twelvemonkeys.image.ResampleOp
import mu.KotlinLogging
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.imageio.ImageIO

private val logger = KotlinLogging.logger {}

object ImageConverter {

    fun downscaleImage(image: ByteArray, height: Int, width: Int): Image {
        val mediaType = ContentDetector.getMediaType(image)
        if (!ContentDetector.isSupportedMediaType(mediaType))
            throw RuntimeException("Unsupported image format $mediaType")

        val bufferedImage = ImageIO.read(image.inputStream())
        val imageDimension = Dimension(height = bufferedImage.height, width = bufferedImage.width)
        if (ContentDetector.isAnimated(mediaType))
            return Image(
                image = image,
                dimension = imageDimension
            )

        val imageType = ContentDetector.toImageType(mediaType)
            ?: return Image(
                image = image,
                dimension = imageDimension
            )

        val preferredDimensions = Dimension(width, height)
        val imageDimensions = Dimension(bufferedImage.width, bufferedImage.height)
        if (imageDimensions.width <= preferredDimensions.width && imageDimensions.height <= preferredDimensions.height)
            return Image(
                image = image,
                dimension = imageDimension
            )

        val scaleTo = scaleImageDimension(imageDimensions, preferredDimensions)
        val sampler = ResampleOp(scaleTo.width, scaleTo.height, ResampleOp.FILTER_LANCZOS)
        val resampled = sampler.filter(bufferedImage, null)

        val outputStream = ByteArrayOutputStream()
        ImageIO.write(resampled, imageType.imageIOFormat, outputStream)
        return Image(
            image = outputStream.toByteArray(),
            dimension = Dimension(
                height = resampled.height,
                width = resampled.width
            ),
            scaled = true
        )
    }

    fun scaleImageDimension(from: Dimension, to: Dimension): Dimension {
        val bestRatio = (to.width / from.width.toFloat()).coerceAtMost(from.height / to.height.toFloat())
        return Dimension(
            width = (from.width * bestRatio).toInt(),
            height = (from.height * bestRatio).toInt()
        )
    }

    fun getDimension(data: ByteArray): Dimension? = getDimension(data.inputStream())

    fun getDimension(stream: InputStream): Dimension? =
        try {
            ImageIO.createImageInputStream(stream).use { fis ->
                val readers = ImageIO.getImageReaders(fis)
                if (readers.hasNext()) {
                    val reader = readers.next()
                    reader.input = fis
                    Dimension(reader.getWidth(0), reader.getHeight(0))
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            logger.error(e) { }
            null
        }
}

class Image(
    val image: ByteArray,
    val dimension: Dimension,
    val scaled: Boolean = false,
)