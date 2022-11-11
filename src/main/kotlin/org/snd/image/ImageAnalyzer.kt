package org.snd.image

import mu.KotlinLogging
import java.io.InputStream
import javax.imageio.ImageIO

private val logger = KotlinLogging.logger {}

object ImageAnalyzer {

    fun getDimension(stream: InputStream): Dimension? =
        try {
            ImageIO.createImageInputStream(stream).use { fis ->
                val readers = ImageIO.getImageReaders(fis)
                if (readers.hasNext()) {
                    val reader = readers.next()
                    reader.input = fis
                    val test = Dimension(reader.getWidth(0).toFloat(), reader.getHeight(0).toFloat())
                    logger.info { "image dimensions $test" }
                    test
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            logger.error(e) { }
            null
        }
}