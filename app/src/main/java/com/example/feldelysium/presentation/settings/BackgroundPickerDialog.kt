package com.example.feldelysium.presentation.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import android.widget.VideoView
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material3.FilledTonalIconToggleButton
import coil.compose.AsyncImage
import com.example.feldelysium.data.settings.AppSettings
import com.example.feldelysium.data.settings.BackgroundKind
import com.example.feldelysium.presentation.playback.BackgroundVideo

private val Presets = longArrayOf(
    AppSettings.DEFAULT_BACKGROUND_COLOR,
    0xFF000000L,
    0xFFFFFFFFL,
    0xFF1B1B2FL,
    0xFF4A2C6AL,
    0xFF8B1E3FL,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackgroundPickerDialog(
    settings: AppSettings,
    onKind: (BackgroundKind) -> Unit,
    onColor: (Long) -> Unit,
    onMedia: (uri: String, isVideo: Boolean) -> Unit,
    onVideoSound: (Boolean) -> Unit,
    onVideoFill: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    val imagePicker = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) onMedia(uri.toString(), false)
    }
    val videoPicker = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) onMedia(uri.toString(), true)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Background")
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = settings.backgroundKind == BackgroundKind.MEDIA,
                        onClick = { onKind(BackgroundKind.MEDIA) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    ) { Text("Media") }
                    SegmentedButton(
                        selected = settings.backgroundKind == BackgroundKind.COLOR,
                        onClick = { onKind(BackgroundKind.COLOR) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    ) { Text("Color") }
                }
                if (settings.backgroundKind == BackgroundKind.MEDIA) {
                    MediaPane(settings, imagePicker, videoPicker, onVideoSound, onVideoFill)
                } else {
                    ColorPane(settings.backgroundColorArgb, onColor)
                }
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End),
                ) { Text("Save") }
            }
        }
    }
}

@Composable
private fun MediaPane(
    settings: AppSettings,
    imagePicker: ActivityResultLauncher<PickVisualMediaRequest>,
    videoPicker: ActivityResultLauncher<PickVisualMediaRequest>,
    onVideoSound: (Boolean) -> Unit,
    onVideoFill: (Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedButton(onClick = {
            imagePicker.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        }) { Text("Image") }
        OutlinedButton(onClick = {
            videoPicker.launch(PickVisualMediaRequest(PickVisualMedia.VideoOnly))
        }) { Text("Video") }
        if (settings.backgroundMediaIsVideo && settings.backgroundMediaUri.isNotBlank()) {
            IconButton(onClick = { onVideoSound(!settings.backgroundVideoSound) }) {
                Icon(
                    if (settings.backgroundVideoSound) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
                    contentDescription = "Sound",
                )
            }
            IconButton(onClick = { onVideoFill(!settings.backgroundVideoFill) }) {
                Icon(
                    if (settings.backgroundVideoFill) Icons.Filled.FitScreen else Icons.Filled.AspectRatio,
                    contentDescription = "Fill",
                )
            }
        }
    }
    MediaPreview(settings)
}

@Composable
private fun MediaPreview(settings: AppSettings) {
    val shape = RoundedCornerShape(8.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(shape)
            .background(Color(settings.backgroundColorArgb.toInt())),
    ) {
        val uri = settings.backgroundMediaUri
        if (uri.isBlank()) return@Box

        if (settings.backgroundMediaIsVideo) {
            BackgroundVideo(
                uri = uri,
                sound = settings.backgroundVideoSound,
                fill = settings.backgroundVideoFill,
                modifier = Modifier.matchParentSize(),
            )
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

@Composable
private fun ColorPane(argb: Long, onColor: (Long) -> Unit) {
    val color = Color(argb.toInt())
    val hsv = remember(argb) {
        FloatArray(3).also { android.graphics.Color.colorToHSV(color.toArgb(), it) }
    }
    var hue by remember(argb) { mutableFloatStateOf(hsv[0]) }
    var value by remember(argb) { mutableFloatStateOf(hsv[2]) }

    fun emit(h: Float, v: Float) {
        onColor(android.graphics.Color.HSVToColor(floatArrayOf(h, 1f, v)).toLong() and 0xFFFFFFFFL)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color)
                .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Presets.forEach { preset ->
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(preset.toInt()))
                        .clickable { onColor(preset) },
                )
            }
        }
    }
    Slider(value = hue, onValueChange = { hue = it; emit(it, value) }, valueRange = 0f..360f)
    Slider(value = value, onValueChange = { value = it; emit(hue, it) })
}
