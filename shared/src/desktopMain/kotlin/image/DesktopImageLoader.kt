package image

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import image.ScaledImage.AnimatedImage
import image.ScaledImage.StaticImage
import image.ScaledImagesDiskCache.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import okhttp3.OkHttpClient
import org.jetbrains.compose.resources.LoadState
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType.UNPREMUL
import org.jetbrains.skia.ColorInfo
import org.jetbrains.skia.ColorSpace.Companion.sRGB
import org.jetbrains.skia.ColorType.BGRA_8888
import org.jetbrains.skia.ImageInfo
import ui.chat.ChatState
import ui.chat.ChatState.EmoteDimensions
import ui.common.AppTheme
import ui.platform.Tooltip
import java.awt.image.BufferedImage
import java.nio.ByteBuffer
import java.util.*

private val logger = KotlinLogging.logger {}

class DesktopImageLoader(
    client: OkHttpClient,
) : ImageLoader {
    private val networkImageLoader = NetworkImageLoader(client)
    private val cache = ScaledImagesDiskCache().apply { initialize() }

    @Composable
    override fun LoadEmoteImage(
        emote: ChatState.Emote,
        emoteDimensions: EmoteDimensions,
        maxHeight: Int?,
        maxWidth: Int?,
    ) {
        val loadingState: MutableState<LoadState<ScaledImage>> =
            remember(maxHeight, maxWidth) { mutableStateOf(LoadState.Loading()) }

        LaunchedEffect(maxHeight, maxWidth, emote.url) {
            withContext(Dispatchers.IO) {
                loadingState.value = loadImage(emote, emoteDimensions, maxHeight, maxWidth)
            }
        }

        when (val imageState = loadingState.value) {
            is LoadState.Loading -> EmoteTooltip(emote) { Loading() }
            is LoadState.Success -> EmoteTooltip(emote) { EmoteImage(emote, imageState.value) }
            is LoadState.Error -> EmoteTooltip(emote) { Error("emote load error") }
        }
    }

    @Composable
    private fun EmoteTooltip(
        emote: ChatState.Emote,
        content: @Composable () -> Unit
    ) {
        DisableSelection { // workaround for selectable container https://github.com/JetBrains/compose-jb/issues/2055
            Tooltip(
                tooltip = {
                    Text(emote.name, modifier = Modifier.background(color = AppTheme.colors.backgroundLight))
                },
                delayMillis = 300
            ) {
                content()
            }
        }
    }

    @Composable
    private fun Loading() {
        DisableSelection {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth().padding(20.dp)
            ) { CircularProgressIndicator() }
        }
    }

    @Composable
    private fun Error(err: String) {
        DisableSelection {
            Text(
                text = err,
                style = TextStyle(color = MaterialTheme.colors.error, fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
        }
    }

    @Composable
    private fun animateImage(
        frames: List<ImageBitmapFrame>
    ): ImageBitmap {
        val transition = rememberInfiniteTransition()
        val frameIndex by transition.animateValue(
            initialValue = 0,
            targetValue = frames.size - 1,
            Int.VectorConverter,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 0
                    for ((index, frame) in frames.withIndex()) {
                        index at durationMillis
                        val frameDuration = if (frame.delay == 0) 100 else frame.delay

                        durationMillis += frameDuration
                    }
                }
            )
        )

        val currentFrame = remember(frameIndex) { frames[frameIndex] }
        return currentFrame.bitmap
    }

    @Composable
    private fun EmoteImage(
        emote: ChatState.Emote,
        image: ScaledImage,
    ) {
        val bitmap = when (image) {
            is AnimatedImage -> animateImage(image.frames)
            is StaticImage -> image.bitmap
        }

        Image(
            bitmap = bitmap,
            contentDescription = emote.name,
            contentScale = ContentScale.Fit,
            filterQuality = FilterQuality.High,
            modifier = Modifier.fillMaxSize()
        )
    }

    private suspend fun loadImage(
        emote: ChatState.Emote,
        dimensions: EmoteDimensions,
        maxHeight: Int?,
        maxWidth: Int?,
    ): LoadState<ScaledImage> {
        val cached = withContext(Dispatchers.IO) {
            loadFromCache(emote, dimensions, maxHeight, maxWidth)
        }
        if (cached != null) {
            logger.debug { "loading scaled image from disk cache ${emote.url}" }
            return LoadState.Success(cached)
        }


        return when (val image = networkImageLoader.getImage(emote.url)) {
            is Result.Error -> {
                dimensions.height = 60
                dimensions.width = 60
                LoadState.Error(image.exception)
            }

            is Result.Success -> {
                return try {
                    LoadState.Success(processImage(emote.url, image.data, dimensions, maxHeight, maxWidth))
                } catch (e: Exception) {
                    LoadState.Error(e)
                }
            }
        }
    }

    private fun loadFromCache(
        emote: ChatState.Emote,
        dimensions: EmoteDimensions,
        maxHeight: Int?,
        maxWidth: Int?,
    ): ScaledImage? {
        val key = CacheKey(emote.url, maxHeight, maxWidth)
        val cachedStatic = cache.getScaledImage(key)?.also {
            dimensions.height = it.height
            dimensions.width = it.width
        }

        if (cachedStatic != null) {
            val bitmap = createBitmap(
                cachedStatic.pixels,
                cachedStatic.width,
                cachedStatic.height
            )

            return StaticImage(bitmap.asComposeImageBitmap())
        }

        val cachedFrames = cache.getScaledImageFrames(key)?.also {
            dimensions.height = it.height
            dimensions.width = it.width
        }

        if (cachedFrames != null) {
            val frames = cachedFrames.frames
                .map {
                    ImageBitmapFrame(
                        bitmap = createBitmap(it.pixels, it.width, it.height).asComposeImageBitmap(),
                        delay = it.delay
                    )
                }

            return AnimatedImage(frames = frames)
        }

        return null
    }

    private fun createBitmap(pixels: ByteArray, width: Int, height: Int): Bitmap {
        val colorInfo = ColorInfo(
            BGRA_8888,
            UNPREMUL,
            sRGB
        )
        val imageInfo = ImageInfo(colorInfo, width, height)
        val bitmap = Bitmap().apply { allocPixels(imageInfo) }
        bitmap.installPixels(pixels)
        return bitmap
    }

    private fun processImage(
        emoteUrl: String,
        image: ByteArray,
        dimensions: EmoteDimensions,
        maxHeight: Int?,
        maxWidth: Int?,
    ): ScaledImage {
        return when (ContentDetector.getMediaType(image)) {
            "image/gif" -> scaleGifImage(emoteUrl, dimensions, image, maxHeight, maxWidth)
            else -> scaleStaticImage(emoteUrl, dimensions, image, maxHeight, maxWidth)
        }
    }

    private fun scaleStaticImage(
        emoteUrl: String,
        dimensions: EmoteDimensions,
        image: ByteArray,
        maxHeight: Int?,
        maxWidth: Int?,
    ): StaticImage {
        val scaled = ImageConverter.scaleImage(image, height = maxHeight, width = maxWidth)
        val pixels = scaled.toBgra8888()

        cache.putScaledImage(
            CacheKey(emoteUrl, scaleMaxWidth = maxWidth, scaleMaxHeight = maxHeight),
            CacheImage(pixels, width = scaled.width, height = scaled.height)
        )
        dimensions.width = scaled.width
        dimensions.height = scaled.height
        return StaticImage(createBitmap(pixels, scaled.width, scaled.height).asComposeImageBitmap())
    }

    private fun scaleGifImage(
        emoteUrl: String,
        dimensions: EmoteDimensions,
        image: ByteArray,
        maxHeight: Int?,
        maxWidth: Int?,
    ): AnimatedImage {
        val scaled = ImageConverter.scaleGif(image, height = maxHeight, width = maxWidth)
        val cacheFrames = scaled.frames.map { frame ->
            CacheImageFrame(
                pixels = frame.data.toBgra8888(),
                width = frame.data.width,
                height = frame.data.height,
                delay = frame.delay
            )
        }

        val bitmapFrames = cacheFrames.map {
            ImageBitmapFrame(
                bitmap = createBitmap(it.pixels, width = it.width, height = it.height).asComposeImageBitmap(),
                delay = it.delay
            )
        }

        cache.putScaledImageFrames(
            CacheKey(emoteUrl, scaleMaxWidth = maxWidth, scaleMaxHeight = maxHeight),
            CacheImageFrames(
                frames = cacheFrames,
                width = scaled.width,
                height = scaled.height
            )

        )

        dimensions.width = scaled.width
        dimensions.height = scaled.height

        return AnimatedImage(frames = bitmapFrames)
    }

    private fun argbToBgraArray(rgbArray: IntArray): ByteArray {
        val bgrArray = Arrays.stream(rgbArray).parallel().map {
            argbToBgra(it)
        }.toArray()
        val byteBuffer = ByteBuffer.allocate(bgrArray.size * 4)
        val intBuffer = byteBuffer.asIntBuffer()
        intBuffer.put(bgrArray)
        return byteBuffer.array()
    }

    private fun argbToBgra(color: Int): Int {
        val uColor = color.toUInt()
        return (((uColor and 0xFF000000u) shr 24) or
                ((uColor and 0x00FF0000u) shr 8) or
                ((uColor and 0x0000FF00u) shl 8) or
                ((uColor and 0x000000FFu) shl 24))
            .toInt()
    }

    private fun BufferedImage.toBgra8888() =
        argbToBgraArray(getRGB(0, 0, width, height, null, 0, width))
}

sealed class ScaledImage {
    class AnimatedImage(
        val frames: List<ImageBitmapFrame>,
    ) : ScaledImage()

    class StaticImage(
        val bitmap: ImageBitmap
    ) : ScaledImage()
}


class ImageBitmapFrame(
    val bitmap: ImageBitmap,
    val delay: Int,
)