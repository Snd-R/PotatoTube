package image

import kotlinx.coroutines.suspendCancellableCoroutine
import mu.KotlinLogging
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private val logger = KotlinLogging.logger {}

class NetworkImageLoader(
    private val client: OkHttpClient
) {

    private val cache: DiskCache = DiskCache().apply { initialize() }

    suspend fun getImage(url: String): Result<ByteArray> {
        return try {
            val cached = cache.getImage(url)
            if (cached != null) {
                logger.debug { "loading image from disk cache $url" }
                return Result.Success(cached)
            }
            logger.debug { "loading image from network $url" }
            val image = loadImageFromNetwork(url)
            return Result.Success(image)
        } catch (e: Exception) {
            logger.error(e) { }
            Result.Error(e)
        }
    }

    private suspend fun loadImageFromNetwork(url: String): ByteArray {
        val request = Request.Builder().url(url.toHttpUrl()).build()
        val call = client.newCall(request)
        return suspendCancellableCoroutine { continuation ->
            call.enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.bytes() ?: throw BadResponse()
                    if (ContentDetector.isSupported(body)) {
                        cache.addImage(url, body)
                        continuation.resume(body)
                    } else
                        continuation.resumeWithException(RuntimeException("Unsupported image format"))
                }

                override fun onFailure(call: Call, e: IOException) {
                    if (continuation.isCancelled) return
                    continuation.resumeWithException(e)
                }
            })

            continuation.invokeOnCancellation {
                try {
                    call.cancel()
                } catch (ex: Throwable) {
                    //Ignore cancel exception
                }
            }
        }
    }
}

sealed class Result<out R> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
}

class BadResponse : RuntimeException()
