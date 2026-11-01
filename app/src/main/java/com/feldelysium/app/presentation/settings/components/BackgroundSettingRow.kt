package com.feldelysium.app.presentation.settings.components

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.feldelysium.app.R
import com.feldelysium.app.domain.model.settings.AppSettings
import com.feldelysium.app.domain.model.settings.BackgroundKind
import com.feldelysium.app.presentation.playback.components.BackgroundVideo
import com.feldelysium.designsystem.components.content.FeldIcon
import com.feldelysium.designsystem.components.content.FeldIconId
import com.feldelysium.designsystem.components.input.feldClickable
import com.feldelysium.designsystem.theme.FeldPalette
import com.feldelysium.designsystem.theme.FeldTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun FeldBackgroundRow(
    settings: AppSettings,
    currentLabel: String,
    modifier: Modifier = Modifier,
    freezeVideo: Boolean = false,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(2.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .feldClickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(64.dp)
                .clip(shape)
                .background(Color(settings.backgroundColorArgb.toInt()))
                .border(1.dp, Color(0xFF3A3D3C), shape),
        ) {
            val uri = settings.backgroundMediaUri
            if (settings.backgroundKind == BackgroundKind.MEDIA && uri.isNotBlank()) {
                if (settings.backgroundMediaIsVideo) {
                    VideoThumbnail(uri, Modifier.matchParentSize())
                    if (!freezeVideo) {
                        BackgroundVideo(
                            uri = uri,
                            sound = false,
                            fill = true,
                            modifier = Modifier.matchParentSize(),
                        )
                    }
                } else {
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize(),
                    )
                }
            }
        }

        Column(Modifier.width(96.dp)) {
            Text(
                text = stringResource(R.string.common_current),
                style = MaterialTheme.typography.labelSmall,
                color = FeldPalette.Muted,
            )
            Text(
                text = currentLabel.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        FeldIcon(
            id = FeldIconId.Chevron,
            size = 12.dp,
            modifier = Modifier.padding(start = 6.dp),
            seed = currentLabel.hashCode() + 1,
        )
    }
}

@Composable
private fun VideoThumbnail(uri: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val frame by produceState<ImageBitmap?>(initialValue = null, uri) {
        value = withContext(Dispatchers.IO) {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, uri.toUri())
                val bitmap: Bitmap? =
                    retriever.getFrameAtTime(0L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                bitmap?.asImageBitmap()
            } catch (_: Exception) {
                null
            } finally {
                runCatching { retriever.release() }
            }
        }
    }
    frame?.let {
        Image(
            bitmap = it,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier,
        )
    }
}

@Preview(widthDp = 380, heightDp = 420, showBackground = true, backgroundColor = 0xFF0B1011)
@Composable
private fun FeldSettingRowPreview() {
    FeldTheme {
        FeldBackgroundRow(
            settings = AppSettings(),
            currentLabel = "Color",
            onClick = {},
        )
    }
}
