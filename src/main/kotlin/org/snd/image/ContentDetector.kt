package org.snd.image

import org.apache.tika.config.TikaConfig
import org.apache.tika.metadata.Metadata

object ContentDetector {

    private val tika: TikaConfig = TikaConfig()

    fun isSupportedImage(data: ByteArray): Boolean {
        val mediaType = tika.detector.detect(data.inputStream(), Metadata()).toString()
        return mediaType.startsWith("image/")
    }
}