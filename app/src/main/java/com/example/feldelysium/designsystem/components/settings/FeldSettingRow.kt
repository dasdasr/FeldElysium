package com.example.feldelysium.designsystem.components.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.feldelysium.data.settings.AppSettings
import com.example.feldelysium.data.settings.BackgroundKind
import com.example.feldelysium.designsystem.components.FeldPalette
import com.example.feldelysium.designsystem.components.FeldSwitch
import com.example.feldelysium.designsystem.components.feldClickable
import com.example.feldelysium.presentation.playback.BackgroundVideo
import com.example.feldelysium.theme.DiscoDialogueTheme

@Composable
fun FeldSettingRow(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
    showChevron: Boolean = false,
    role: Role = Role.Button,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
    trailing: (@Composable () -> Unit)? = null,
) {
    val clickable = if (enabled && onClick != null) {
        Modifier.feldClickable(role = role, onClick = onClick)
    } else Modifier

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(clickable)
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .alpha(if (enabled) 1f else 0.4f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = FeldPalette.Muted,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = FeldPalette.Muted,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!enabled) {
                SoonStamp()
            } else {
                trailing?.invoke()
                if (showChevron) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = FeldPalette.Muted,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(22.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun FeldSettingValueRow(
    icon: ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    FeldSettingRow(
        icon = icon,
        title = title,
        subtitle = subtitle,
        modifier = modifier,
        enabled = enabled,
        showChevron = true,
        onClick = onClick,
        trailing = {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 8.dp),
            )
        },
    )
}

@Composable
fun FeldSettingSwitchRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
    seed: Int = 0,
) {
    FeldSettingRow(
        icon = icon,
        title = title,
        subtitle = subtitle,
        modifier = modifier,
        enabled = enabled,
        role = Role.Switch,
        onClick = { onCheckedChange(!checked) },
        trailing = {
            FeldSwitch(
                checked = checked,
                onCheckedChange = null,
                seed = seed,
            )
        },
    )
}

@Composable
fun FeldBackgroundRow(
    settings: AppSettings,
    currentLabel: String,
    modifier: Modifier = Modifier,
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
                    BackgroundVideo(
                        uri = uri,
                        sound = false,
                        fill = true,
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
private fun SoonStamp() {
    Text(
        text = "SOON",
        style = MaterialTheme.typography.labelSmall,
        color = FeldPalette.Muted,
        modifier = Modifier
            .border(1.dp, FeldPalette.Muted.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

@Preview(widthDp = 380, heightDp = 420, showBackground = true, backgroundColor = 0xFF0B1011)
@Composable
private fun FeldSettingRowPreview() {
    DiscoDialogueTheme {
        Column {
            FeldSettingSwitchRow(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                title = "Cinema frame",
                subtitle = "Keep playback inside 16:9",
                checked = true,
                onCheckedChange = {},
                seed = 1,
            )
            FeldSettingValueRow(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                title = "Typeface",
                value = "Sina Nova Regular",
                onClick = {},
            )
            FeldSettingRow(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                title = "Autoplay",
                subtitle = "Play scene automatically",
                enabled = false,
            )
            FeldBackgroundRow(
                settings = AppSettings(),
                currentLabel = "Color",
                onClick = {},
            )
        }
    }
}
