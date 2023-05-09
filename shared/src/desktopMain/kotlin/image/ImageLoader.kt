package image

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import okhttp3.OkHttpClient
import org.jetbrains.compose.animatedimage.AnimatedImage
import org.jetbrains.compose.animatedimage.animate
import org.jetbrains.compose.resources.LoadState
import org.jetbrains.skia.Codec
import org.jetbrains.skia.Data
import ui.chat.ChatState
import ui.common.AppTheme
import ui.platform.Tooltip

private val logger = KotlinLogging.logger {}

class DesktopImageLoader(
    client: OkHttpClient,
) : ImageLoader {
    private val imageLoader = ImageLoaderDesktop(client)

    @Composable
    override fun LoadEmoteImage(
        emote: ChatState.Emote,
        emoteDimensions: ChatState.EmoteDimensions,
        maxHeight: Int?,
        maxWidth: Int?,
    ) {
        val loadingState: MutableState<LoadState<AnimatedImage>> =
            remember(maxHeight, maxWidth) { mutableStateOf(LoadState.Loading()) }

        LaunchedEffect(maxHeight, maxWidth, emote.url) {
            withContext(Dispatchers.IO) {
                loadingState.value = loadImage(emote, emoteDimensions, maxHeight, maxWidth)
            }
        }

        when (val imageState = loadingState.value) {
            is LoadState.Loading -> EmoteTooltip(emote) { Loading() }
            is LoadState.Success -> EmoteTooltip(emote) { EmoteImage(emote, imageState.value.animate()) }
            is LoadState.Error -> EmoteTooltip(emote) { Error("emote load error") }
        }
    }

    private suspend fun loadImage(
        emote: ChatState.Emote,
        dimensions: ChatState.EmoteDimensions,
        maxHeight: Int?,
        maxWidth: Int?,
    ): LoadState<AnimatedImage> {
        try {
            return when (val image = imageLoader.getImage(emote.url)) {
                is Result.Error -> {
                    dimensions.height = 60
                    dimensions.width = 60
                    LoadState.Error(image.exception)
                }

                is Result.Success -> {
                    LoadState.Success(scaleImage(image.data, dimensions, maxHeight, maxWidth))
                }
            }
        } catch (e: Exception) {
            logger.error(e) {}
            dimensions.height = 60
            dimensions.width = 60
            return LoadState.Error(e)
        }
    }

    @Composable
    fun EmoteImage(
        emote: ChatState.Emote,
        image: ImageBitmap,
    ) {
        Image(
            bitmap = image,
            contentDescription = emote.name,
            contentScale = ContentScale.Fit,
            filterQuality = FilterQuality.High,
            modifier = Modifier.fillMaxSize()
        )
    }

    @Composable
    fun EmoteTooltip(
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
    fun Loading() {
        DisableSelection {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth().padding(20.dp)
            ) { CircularProgressIndicator() }
        }
    }

    @Composable
    fun Error(err: String) {
        DisableSelection {
            Text(
                text = err,
                style = TextStyle(color = MaterialTheme.colors.error, fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
        }
    }


    private fun scaleImage(
        image: ByteArray,
        dimensions: ChatState.EmoteDimensions,
        maxHeight: Int?,
        maxWidth: Int?,
    ): AnimatedImage {
        if (maxHeight == null && maxWidth == null) return image.toAnimatedImage()

        return runCatching {
            val scaled = ImageConverter.scaleImage(image, height = maxHeight, width = maxWidth)
            dimensions.height = scaled.dimensions.height
            dimensions.width = scaled.dimensions.width
            return scaled.image.toAnimatedImage()
        }
            .onFailure { logger.error(it) { } }
            .getOrDefault(image.toAnimatedImage())
    }
}

private fun ByteArray.toAnimatedImage(): AnimatedImage {
    val data = Data.makeFromBytes(this)
    val codec = Codec.makeFromData(data)
    return AnimatedImage(codec)
}
