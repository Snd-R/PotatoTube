package image

import com.madgag.gif.fmsware.AnimatedGifEncoder
import com.madgag.gif.fmsware.GifDecoder
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

        if (ContentDetector.isGif(mediaType)) {
            return scaleGif(image, height, width)
        }

        val imageType = ContentDetector.toImageType(mediaType)
            ?: throw IllegalStateException("Can't detect image type before scaling")

        val resampled = scaleBufferedImage(ImageIO.read(image.inputStream()), height, width)

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

    private fun scaleGif(image: ByteArray, height: Int?, width: Int?): Image {
        val decoder = GifDecoder()
        val encoder = AnimatedGifEncoder()
        decoder.read(image.inputStream())

        val outputStream = ByteArrayOutputStream()
        encoder.start(outputStream)

        val firstFrame = scaleBufferedImage(decoder.getFrame(0), height, width)
        encoder.setDelay(decoder.getDelay(0))
        encoder.addFrame(firstFrame)
        for (i in 1..<decoder.frameCount) {
            val frame = decoder.getFrame(i)
            val resampled = scaleBufferedImage(frame, height, width)
            encoder.setDelay(decoder.getDelay(i))
            encoder.addFrame(resampled)
        }
        encoder.finish()

        return Image(
            image = outputStream.toByteArray(),
            dimensions = Dimensions(
                height = firstFrame.height,
                width = firstFrame.width
            ),
        )
    }

    private fun scaleBufferedImage(image: BufferedImage, height: Int?, width: Int?): BufferedImage {
        val scaleTo = when {
            height != null && width != null -> scaleDimensions(image, height, width)
            height != null -> scaleDimensionsByMaxHeight(image, height)
            else -> scaleDimensionsByMaxWidth(image, width!!)
        }
        return ResampleOp(scaleTo.width, scaleTo.height, ResampleOp.FILTER_LANCZOS)
            .filter(image, null)
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
