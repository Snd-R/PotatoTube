@file:Suppress("NewApi")

package image

import dev.dirs.ProjectDirectories
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.io.path.notExists

class DiskCache {
    private val cache = ConcurrentHashMap<String, Path>()

    private val cacheDirectory = Path.of(
        ProjectDirectories.from(
            "org", "snd", "PotatoTube"
        ).cacheDir
    )

    fun getImage(url: String): ByteArray? {
        val cached = cache[getCacheKey(url)] ?: return null
        return Files.readAllBytes(cached)
    }

    fun addImage(url: String, image: ByteArray) {
        val cacheKey = getCacheKey(url)
        val filePath = cacheDirectory.resolve(cacheKey)
        if (filePath.notExists())
            Files.createFile(filePath)

        Files.write(filePath, image)
        cache[cacheKey] = filePath
    }

    fun initialize() {
        Files.createDirectories(cacheDirectory)
        Files.list(cacheDirectory)
            .filter { !it.isDirectory() }
            .forEach { cache[it.name] = it }
    }

    private fun getCacheKey(url: String): String {
        val uri = URI.create(url)
        return "${uri.host}${uri.path.split("/").joinToString("_")}"
    }
}