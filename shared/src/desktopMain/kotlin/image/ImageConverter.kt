package image

import com.twelvemonkeys.image.ResampleOp
import image.ScaledBufferedImageFrames.ScaledBufferedImageFrame
import org.snd.gifdecoder.StandardGifDecoder
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

object ImageConverter {

    fun scaleImage(
        encodedImage: ByteArray,
        height: Int?,
        width: Int?,
    ): BufferedImage {
        if (height == null && width == null) throw IllegalStateException("scale height and width cannot be null")

        val mediaType = ContentDetector.getMediaType(encodedImage)
        if (!ContentDetector.isSupportedMediaType(mediaType))
            throw IllegalStateException("Unsupported image format $mediaType")

        return scaleBufferedImage(ImageIO.read(encodedImage.inputStream()), height, width)
    }

    fun scaleGif(image: ByteArray, height: Int?, width: Int?): ScaledBufferedImageFrames {
        val frames = ArrayList<ScaledBufferedImageFrame>()
        val gifDecoder = StandardGifDecoder()
        gifDecoder.read(image)
        gifDecoder.advance()
        for (i in 0..<gifDecoder.frameCount) {
            val currentFrame = gifDecoder.nextFrame
            val resampled = scaleBufferedImage(currentFrame, height, width)
            val currentFrameIndex = gifDecoder.currentFrameIndex
            val delay = gifDecoder.getDelay(currentFrameIndex)

            frames.add(
                ScaledBufferedImageFrame(
                    data = resampled,
                    delay = delay
                )
            )
            gifDecoder.advance()
        }

        val scaledDimensions = getScaleDimensions(gifDecoder.height, gifDecoder.width, height, width)

        return ScaledBufferedImageFrames(
            frames = frames,
            width = scaledDimensions.width,
            height = scaledDimensions.height
        )
    }

    private fun scaleBufferedImage(image: BufferedImage, height: Int?, width: Int?): BufferedImage {
        val scaleTo = getScaleDimensions(image.height, image.width, height, width)
        return ResampleOp(scaleTo.width, scaleTo.height, ResampleOp.FILTER_LANCZOS)
            .filter(image, null)
    }

    private fun getScaleDimensions(srcHeight: Int, srcWidth: Int, height: Int?, width: Int?): ImageDimensions {
        return when {
            height != null && width != null -> scaleDimensions(srcHeight, srcWidth, height, width)
            height != null -> scaleDimensionsByMaxHeight(srcHeight, srcWidth, height)
            else -> scaleDimensionsByMaxWidth(srcHeight, srcWidth, width!!)
        }
    }

    private fun scaleDimensions(srcHeight: Int, srcWidth: Int, maxHeight: Int, maxWidth: Int): ImageDimensions {
        val bestRatio = (maxWidth / srcWidth.toFloat()).coerceAtMost(maxHeight / srcHeight.toFloat())
        return ImageDimensions(
            width = (srcWidth * bestRatio).toInt(),
            height = (srcHeight * bestRatio).toInt()
        )
    }

    private fun scaleDimensionsByMaxHeight(srcHeight: Int, srcWidth: Int, maxHeight: Int): ImageDimensions {
        val ratio = maxHeight.toDouble() / srcHeight
        return ImageDimensions(
            width = (srcWidth * ratio).toInt(),
            height = (srcHeight * ratio).toInt()
        )
    }

    private fun scaleDimensionsByMaxWidth(srcHeight: Int, srcWidth: Int, maxWidth: Int): ImageDimensions {
        val ratio = maxWidth.toDouble() / srcWidth
        return ImageDimensions(
            width = (srcWidth * ratio).toInt(),
            height = (srcHeight * ratio).toInt()
        )
    }
}

class ScaledBufferedImageFrames(
    val frames: List<ScaledBufferedImageFrame>,
    val width: Int,
    val height: Int,
) {
    class ScaledBufferedImageFrame(
        val data: BufferedImage,
        val delay: Int
    )
}

private class ImageDimensions(
    val width: Int,
    val height: Int
)
