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
import kotlinx.coroutines.launch
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
        dimension: ChatState.EmoteDimension,
        scaleTo: Dimension?
    ) {
        val state: MutableState<LoadState<AnimatedImage>> = remember { mutableStateOf(LoadState.Loading()) }
        LaunchedEffect(emote.url) {
            withContext(Dispatchers.IO) {
                launch {
                    try {
                        when (val image = imageLoader.getImage(emote.url)) {
                            is Result.Error -> {
                                state.value = LoadState.Error(image.exception)
//                                dimension.height = 60
//                                dimension.width = 60
                            }

                            is Result.Success -> {
                                val processed = withContext(Dispatchers.IO) {
                                    scaleImage(
                                        image.data,
//                                        dimension,
                                        scaleTo
                                    )
                                }
                                state.value = LoadState.Success(processed)
                            }
                        }
                    } catch (e: Exception) {
                        logger.error(e) {}
                        state.value = LoadState.Error(e)
//                        dimension.height = 60
//                        dimension.width = 60
                    }
                }
            }
        }

        when (val currentState = state.value) {
            is LoadState.Loading -> EmoteTooltip(emote) { Loader() }
            is LoadState.Error -> EmoteTooltip(emote) { Error("emote load error") }
            is LoadState.Success -> {
                EmoteTooltip(emote) {
                    EmoteImage(
                        emote,
                        currentState.value.animate()
                    )
                }
            }
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
    fun Loader() {
        DisableSelection {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth().padding(20.dp)
            ) {
                CircularProgressIndicator()
            }
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
//        dimension: Chat.EmoteDimension,
        scaleTo: Dimension?,
    ): AnimatedImage {
        val processed = scaleTo?.let {
            ImageConverter.downscaleImage(
                image,
                height = scaleTo.height,
                width = scaleTo.width
            )
        }

        if (processed != null) {
            if (processed.scaled) {
//                dimension.height = processed.dimension.height
//                dimension.width = processed.dimension.width
            } else { //TODO implement proper scaling for other types
                if (processed.dimension.width <= scaleTo.width && processed.dimension.height <= scaleTo.height) {
//                    dimension.height = processed.dimension.height
//                    dimension.width = processed.dimension.width
                } else {
                    val scaledDimension = ImageConverter.scaleImageDimension(processed.dimension, scaleTo)
//                    dimension.height = scaledDimension.height
//                    dimension.width = scaledDimension.width
                }
            }
        } else {
            val scaledDimension = ImageConverter.getDimension(image)
//            dimension.height = scaledDimension?.height
//            dimension.width = scaledDimension?.width
        }

        val data = Data.makeFromBytes(processed?.image ?: image)
//        val data = Data.makeFromBytes( image)
        val codec = Codec.makeFromData(data)
        return AnimatedImage(codec)
    }
}
