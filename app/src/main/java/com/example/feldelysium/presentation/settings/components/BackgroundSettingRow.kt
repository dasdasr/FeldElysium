package com.example.feldelysium.presentation.settings.components

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.feldelysium.data.settings.AppSettings
import com.example.feldelysium.data.settings.BackgroundKind
import com.example.feldelysium.designsystem.components.FeldPalette
import com.example.feldelysium.designsystem.components.feldClickable
import com.example.feldelysium.designsystem.components.settings.FeldSettingRow
import com.example.feldelysium.designsystem.components.settings.FeldSettingSwitchRow
import com.example.feldelysium.designsystem.components.settings.FeldSettingValueRow
import com.example.feldelysium.presentation.playback.components.BackgroundVideo
import com.example.feldelysium.theme.DiscoDialogueTheme
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
                text = "CURRENT",
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

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = FeldPalette.Muted,
            modifier = Modifier.size(22.dp),
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
                retriever.setDataSource(context, Uri.parse(uri))
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
    DiscoDialogueTheme {
        FeldBackgroundRow(
            settings = AppSettings(),
            currentLabel = "Color",
            onClick = {},
        )
    }
}
