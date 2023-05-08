package image

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import ui.chat.ChatState

class AndroidImageLoader : ImageLoader {
    @Composable
    override fun LoadEmoteImage(emote: ChatState.Emote, dimension: ChatState.EmoteDimension, scaleTo: Dimension?) {
        Box {
            val painter = rememberAsyncImagePainter(emote.url)
            Image(
                painter = painter,
                contentDescription = emote.name,
                alignment = Alignment.Center,
                contentScale = ContentScale.Fit,
                alpha = DefaultAlpha,
                modifier = Modifier.fillMaxSize()
            )

            when (painter.state) {
                is AsyncImagePainter.State.Empty, is AsyncImagePainter.State.Success -> {}
                is AsyncImagePainter.State.Loading -> Loader()
                is AsyncImagePainter.State.Error -> Error("emote load error")
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
}