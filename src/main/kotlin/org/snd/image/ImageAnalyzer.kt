package org.snd.image

import java.io.InputStream
import javax.imageio.ImageIO

object ImageAnalyzer {

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
            null
        }
}