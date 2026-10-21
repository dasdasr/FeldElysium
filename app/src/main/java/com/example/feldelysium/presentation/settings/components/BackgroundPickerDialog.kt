package com.example.feldelysium.presentation.settings.components

import android.graphics.Color.HSVToColor
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.feldelysium.data.settings.AppSettings
import com.example.feldelysium.data.settings.BackgroundKind
import com.example.feldelysium.designsystem.components.button.FeldButtonDefaults
import com.example.feldelysium.designsystem.components.card.FeldCard
import com.example.feldelysium.designsystem.components.dialog.FeldDialog
import com.example.feldelysium.designsystem.components.dialog.FeldDialogFooter
import com.example.feldelysium.designsystem.components.display.FeldDivider
import com.example.feldelysium.designsystem.components.display.FeldIcon
import com.example.feldelysium.designsystem.components.display.FeldIconId
import com.example.feldelysium.designsystem.components.input.FeldColorSwatches
import com.example.feldelysium.designsystem.components.input.FeldSegmentedControl
import com.example.feldelysium.designsystem.components.input.FeldSlider
import com.example.feldelysium.designsystem.components.input.FeldSliderColors
import com.example.feldelysium.designsystem.components.input.FeldSwitch
import com.example.feldelysium.designsystem.components.input.swatchSpec
import com.example.feldelysium.designsystem.components.settings.FeldSettingRow
import com.example.feldelysium.designsystem.foundation.material.FeldPaper
import com.example.feldelysium.designsystem.foundation.material.paperMaterial
import com.example.feldelysium.designsystem.foundation.shape.RoughShape
import com.example.feldelysium.designsystem.foundation.shape.RoughStyle
import com.example.feldelysium.designsystem.foundation.utils.FeldPalette
import com.example.feldelysium.presentation.playback.components.BackgroundVideo

private val Presets = listOf(
    0xFF00B140L,
    AppSettings.DEFAULT_BACKGROUND_COLOR,
    0xFF59663AL,
    0xFFC4A57AL,
    0xFFE45720L,
    0xFF3F8F86L,
    0xFF3E67C8L,
    0xFF7C55C9L,
)

private val PreviewRough = RoughStyle(
    roughness = 0.9.dp,
    step = 12.dp,
    corner = 3.dp,
    tornChance = 0.2f,
)

@Composable
fun BackgroundPickerDialog(
    settings: AppSettings,
    onKind: (BackgroundKind) -> Unit,
    onColor: (Long) -> Unit,
    onMedia: (uri: String, isVideo: Boolean) -> Unit,
    onVideoSound: (Boolean) -> Unit,
    onVideoFill: (Boolean) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val imagePicker = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) onMedia(uri.toString(), false)
    }
    val videoPicker = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) onMedia(uri.toString(), true)
    }

    val isMedia = settings.backgroundKind == BackgroundKind.MEDIA
    val animated =
        isMedia && settings.backgroundMediaIsVideo && settings.backgroundMediaUri.isNotBlank()

    FeldDialog(
        title = "Background",
        eyebrow = if (animated) "Animated" else "Plain",
        subtitle = "Set a visual tone for your scene.",
        onDismiss = onDismiss,
        footer = {
            FeldDialogFooter(
                onReset = onReset,
                onCancel = onDismiss,
                confirmLabel = "Save",
                buttonSpec = FeldButtonDefaults.Cream,
            )
        },
    ) {
        FeldSegmentedControl(
            options = listOf("Media", "Color"),
            selectedIndex = if (isMedia) 0 else 1,
            onSelect = { onKind(if (it == 0) BackgroundKind.MEDIA else BackgroundKind.COLOR) },
        )
        Spacer(Modifier.height(16.dp))

        if (isMedia) {
            MediaPane(settings, imagePicker, videoPicker, onVideoSound, onVideoFill)
        } else {
            ColorPane(settings.backgroundColorArgb, onColor)
        }
    }
}

@Suppress("ParamsComparedByRef")
@Composable
private fun MediaPane(
    settings: AppSettings,
    imagePicker: ActivityResultLauncher<PickVisualMediaRequest>,
    videoPicker: ActivityResultLauncher<PickVisualMediaRequest>,
    onVideoSound: (Boolean) -> Unit,
    onVideoFill: (Boolean) -> Unit,
) {
    val rowPadding = PaddingValues(vertical = 14.dp)
    val video = settings.backgroundMediaIsVideo && settings.backgroundMediaUri.isNotBlank()

    Column {
        MediaHeader(video)
        Spacer(Modifier.height(12.dp))
        MediaPreview(settings)
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MediaPickButton(
                iconId = FeldIconId.Video,
                label = "Video",
                onClick = { videoPicker.launch(PickVisualMediaRequest(PickVisualMedia.VideoOnly)) },
                modifier = Modifier.weight(1f),
            )
            MediaPickButton(
                iconId = FeldIconId.Image,
                label = "Image",
                onClick = { imagePicker.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly)) },
                modifier = Modifier.weight(1f),
            )
        }

        if (video) {
            Spacer(Modifier.height(4.dp))
            FeldDivider()
            FeldSettingRow(
                icon = FeldIconId.Voice,
                title = "Mute sound",
                subtitle = "Mute video sound.",
                role = Role.Switch,
                contentPadding = rowPadding,
                onClick = { onVideoSound(!settings.backgroundVideoSound) },
                trailing = {
                    FeldSwitch(checked = !settings.backgroundVideoSound, onCheckedChange = { })
                },
            )
            FeldDivider()
            FeldSettingRow(
                icon = FeldIconId.Cinema,
                title = "Fill screen",
                subtitle = "Crop videos to fit the screen.",
                role = Role.Switch,
                contentPadding = rowPadding,
                onClick = { onVideoFill(!settings.backgroundVideoFill) },
                trailing = {
                    FeldSwitch(checked = settings.backgroundVideoFill, onCheckedChange = { })
                },
            )
            FeldDivider()
        }
    }
}

@Composable
private fun MediaPickButton(
    iconId: FeldIconId,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FeldCard(
        modifier = modifier,
        seed = label.hashCode(),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
        onClick = onClick,
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FeldIcon(id = iconId, modifier = Modifier)
            Spacer(Modifier.width(8.dp))
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun MediaHeader(video: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val iconId = if (video) FeldIconId.Video else FeldIconId.Image
        FeldIcon(
            id = iconId,
            modifier = Modifier,
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = if (video) "VIDEO" else "IMAGE",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (video) {
            Text(
                text = "› › ›",
                style = MaterialTheme.typography.labelLarge,
                color = FeldPalette.Muted,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun MediaPreview(settings: AppSettings) {
    val shape = remember { RoughShape(9, PreviewRough) }
    val uri = settings.backgroundMediaUri

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 10f)
            .paperMaterial(shape, FeldPaper.Field),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .matchParentSize()
                .background(Color(settings.backgroundColorArgb.toInt())),
        )
        if (uri.isBlank()) {
            Text(
                text = "NO MEDIA YET",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Black.copy(alpha = 0.55f),
            )
        } else if (settings.backgroundMediaIsVideo) {
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

private fun hsvOf(argb: Long): FloatArray = FloatArray(3).also { android.graphics.Color.colorToHSV(argb.toInt(), it) }

private fun colorOf(hue: Float, sat: Float, bri: Float): Long =
    HSVToColor(floatArrayOf(hue, sat, bri)).toLong() and 0xFFFFFFFFL

@Composable
private fun ColorPane(argb: Long, onColor: (Long) -> Unit) {
    val initial = remember { hsvOf(argb) }
    var hue by remember { mutableFloatStateOf(initial[0]) }
    var sat by remember { mutableFloatStateOf(initial[1]) }
    var bri by remember { mutableFloatStateOf(initial[2]) }
    var last by remember { mutableLongStateOf(argb) }
    val pending = remember { ArrayDeque<Long>() }

    LaunchedEffect(argb) {
        val idx = pending.indexOf(argb)
        when {
            idx >= 0 -> repeat(idx + 1) { pending.removeFirst() }

            argb != last -> {
                val hsv = hsvOf(argb)
                hue = hsv[0]
                sat = hsv[1]
                bri = hsv[2]
                last = argb
                pending.clear()
            }
        }
    }

    fun commit() {
        val c = colorOf(hue, sat, bri)
        if (c == last) return
        last = c
        pending.addLast(c)
        if (pending.size > 32) pending.removeFirst()
        onColor(c)
    }

    val live = colorOf(hue, sat, bri)
    val chipShape = remember { RoughShape(13, PreviewRough.copy(corner = 4.dp)) }

    Column {
        Box(
            Modifier
                .fillMaxWidth()
                .height(52.dp)
                .paperMaterial(chipShape, swatchSpec(live, selected = true)),
        )

        Spacer(Modifier.height(12.dp))
        FeldSlider(
            value = hue,
            onValueChange = {
                hue = it
                if (sat < 0.1f) sat = 1f
            },
            onValueChangeFinished = { commit() },
            valueRange = 0f..360f,
            accent = FeldSliderColors.Cream,
        )
        FeldSlider(
            value = bri,
            onValueChange = { bri = it },
            onValueChangeFinished = { commit() },
            accent = FeldSliderColors.Teal,
        )
        Spacer(Modifier.height(12.dp))
        FeldColorSwatches(
            colors = Presets,
            selected = argb,
            onSelect = onColor,
        )
    }
}
