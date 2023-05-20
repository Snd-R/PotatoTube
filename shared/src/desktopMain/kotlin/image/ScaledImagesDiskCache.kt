@file:Suppress("NewApi")

package image

import dev.dirs.ProjectDirectories
import org.apache.commons.io.FileUtils
import org.jetbrains.skia.*
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.exists
import kotlin.io.path.notExists

class ScaledImagesDiskCache {
    private val cache = ConcurrentHashMap<String, CachedImage>()
    private val framesCache = ConcurrentHashMap<String, CachedImageFrames>()

    private val cacheDirectory = Path.of(
        ProjectDirectories.from(
            "org", "snd", "PotatoTube"
        ).cacheDir
    )

    fun putScaledImage(key: CacheKey, image: CacheImage) {
        val keyString = getKeyString(key)
        val filePath = cacheDirectory.resolve("scaled/").resolve(keyString)
        if (filePath.notExists())
            Files.createFile(filePath)

        Files.write(filePath, image.pixels)
        cache[keyString] = CachedImage(path = filePath, width = image.width, height = image.height)
    }

    fun putScaledImageFrames(key: CacheKey, frames: CacheImageFrames) {
        val keyString = getKeyString(key)

        val pathToFrameMap = frames.frames
            .mapIndexed { index, frame ->
                val filePath = cacheDirectory.resolve("scaledFrames/")
                    .resolve("${keyString}_${index}")

                filePath to frame
            }

        pathToFrameMap.forEach { (path, frame) ->
            if (path.notExists()) Files.createFile(path)
            Files.write(path, frame.pixels)
        }

        val cacheFrames = pathToFrameMap.associate { (path, frame) ->
            path to CachedImageFrameData(
                delay = frame.delay,
                width = frame.width,
                height = frame.height
            )
        }
        framesCache[keyString] = CachedImageFrames(frames = cacheFrames, width = frames.width, height = frames.height)
    }

    fun getScaledImageFrames(key: CacheKey): CacheImageFrames? {
        val cached = framesCache[getKeyString(key)] ?: return null
        val frames = cached.frames.map { (path, frameData) ->
            CacheImageFrame(
                pixels = Files.readAllBytes(path),
                width = frameData.width,
                height = frameData.height,
                delay = frameData.delay
            )
        }
        return CacheImageFrames(frames = frames, width = cached.width, height = cached.height)
    }

    fun getScaledImage(key: CacheKey): CacheImage? {
        val cached = cache[getKeyString(key)] ?: return null
        val pixels = Files.readAllBytes(cached.path)

        return CacheImage(pixels = pixels, width = cached.width, height = cached.height)
    }

    fun initialize() {
        val scaledDirectory = cacheDirectory.resolve("scaled/")
        val scaledFramesDirectory = cacheDirectory.resolve("scaledFrames/")
        if (scaledDirectory.exists()) FileUtils.forceDelete(scaledDirectory.toFile())
        if (scaledFramesDirectory.exists()) FileUtils.forceDelete(scaledFramesDirectory.toFile())

        Files.createDirectories(cacheDirectory.resolve("scaled/"))
        Files.createDirectories(cacheDirectory.resolve("scaledFrames/"))
    }

    private fun ByteArray.toBitmap(width: Int, height: Int): Bitmap {
        val colorInfo = ColorInfo(
            ColorType.BGRA_8888,
            ColorAlphaType.UNPREMUL,
            ColorSpace.sRGB
        )
        val imageInfo = ImageInfo(colorInfo, width, height)
        val bitmap = Bitmap().apply { allocPixels(imageInfo) }
        bitmap.installPixels(this)
        return bitmap
    }

    private fun getKeyString(key: CacheKey): String {
        val uri = URI.create(key.url)
        return "${uri.host}${uri.path.split("/").joinToString("_")}_${key.scaleMaxWidth}_${key.scaleMaxHeight}"
    }

    class CacheKey(
        val url: String,
        val scaleMaxWidth: Int?,
        val scaleMaxHeight: Int?,
    )

    class CacheImage(
        val pixels: ByteArray,
        val width: Int,
        val height: Int,
    )

    class CacheImageFrames(
        val frames: List<CacheImageFrame>,
        val width: Int,
        val height: Int,
    )

    class CacheImageFrame(
        val pixels: ByteArray,
        val width: Int,
        val height: Int,
        val delay: Int
    )

    private class CachedImage(
        val path: Path,
        val width: Int,
        val height: Int,
    )

    private class CachedImageFrames(
        val frames: Map<Path, CachedImageFrameData>,
        val width: Int,
        val height: Int,
    )

    private class CachedImageFrameData(
        val delay: Int,
        val width: Int,
        val height: Int,
    )
}

