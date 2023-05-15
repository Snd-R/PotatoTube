package image

import org.apache.tika.config.TikaConfig
import org.apache.tika.metadata.Metadata

object ContentDetector {

    private val tika: TikaConfig = TikaConfig()

    fun getMediaType(data: ByteArray) = tika.detector.detect(data.inputStream(), Metadata()).toString()

    fun isSupported(data: ByteArray) = isSupportedMediaType(getMediaType(data))

    fun isSupportedMediaType(type: String) = type.startsWith("image/")

    fun isGif(type: String) = type == "image/gif"

    fun toImageType(mediaType: String) = ImageType.values().firstOrNull { it.mediaType == mediaType }
}