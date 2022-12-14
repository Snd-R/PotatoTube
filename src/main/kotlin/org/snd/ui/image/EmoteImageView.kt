package org.snd.ui.image

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import org.jetbrains.compose.animatedimage.AnimatedImage
import org.jetbrains.compose.animatedimage.animate
import org.jetbrains.compose.resources.LoadState
import org.jetbrains.skia.Codec
import org.jetbrains.skia.Data
import org.snd.image.Dimension
import org.snd.image.ImageConverter
import org.snd.image.Result
import org.snd.ui.chat.Chat
import org.snd.ui.common.AppTheme

private val logger = KotlinLogging.logger {}

@Composable
fun EmoteImage(
    emote: Chat.Emote,
    dimension: Chat.EmoteDimension,
    model: Chat,
    scaleTo: Dimension? = null,
) {
    val state: MutableState<LoadState<AnimatedImage>> = remember { mutableStateOf(LoadState.Loading()) }
    LaunchedEffect(emote.url) {
        withContext(Dispatchers.IO) {
            launch {
                try {
                    when (val image = model.imageLoader.getImage(emote.url)) {
                        is Result.Error -> {
                            state.value = LoadState.Error(image.exception)
                            dimension.height = 60
                            dimension.width = 60
                        }

                        is Result.Success -> {
                            val processed = withContext(Dispatchers.IO) {
                                scaleImage(image.data, dimension, scaleTo)
                            }
                            state.value = LoadState.Success(processed)
                        }
                    }
                } catch (e: Exception) {
                    logger.error(e) {}
                    state.value = LoadState.Error(e)
                    dimension.height = 60
                    dimension.width = 60
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

private fun scaleImage(
    image: ByteArray,
    dimension: Chat.EmoteDimension,
    scaleTo: Dimension? = null,
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
            dimension.height = processed.dimension.height
            dimension.width = processed.dimension.width
        } else { //TODO implement proper scaling for other types
            if (processed.dimension.width <= scaleTo.width && processed.dimension.height <= scaleTo.height) {
                dimension.height = processed.dimension.height
                dimension.width = processed.dimension.width
            } else {
                val scaledDimension = ImageConverter.scaleImageDimension(processed.dimension, scaleTo)
                dimension.height = scaledDimension.height
                dimension.width = scaledDimension.width
            }
        }
    } else {
        val scaledDimension = ImageConverter.getDimension(image)
        dimension.height = scaledDimension?.height
        dimension.width = scaledDimension?.width
    }

    val data = Data.makeFromBytes(processed?.image ?: image)
    val codec = Codec.makeFromData(data)
    return AnimatedImage(codec)
}

@Composable
fun EmoteImage(
    emote: Chat.Emote,
    image: ImageBitmap,
) {
    Image(
        bitmap = image,
        contentDescription = emote.name,
        contentScale = ContentScale.Inside,
        filterQuality = FilterQuality.None,
        modifier = Modifier.fillMaxSize()
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EmoteTooltip(
    emote: Chat.Emote,
    content: @Composable () -> Unit
) {
    DisableSelection { // workaround for selectable container https://github.com/JetBrains/compose-jb/issues/2055
        TooltipArea(
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
