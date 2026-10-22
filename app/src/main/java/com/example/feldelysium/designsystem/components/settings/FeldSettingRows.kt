package com.example.feldelysium.designsystem.components.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.feldelysium.designsystem.components.display.FeldIcon
import com.example.feldelysium.designsystem.components.display.FeldIconId
import com.example.feldelysium.designsystem.components.input.FeldSwitch
import com.example.feldelysium.designsystem.foundation.seed.rememberSeed
import com.example.feldelysium.designsystem.foundation.utils.FeldPalette
import com.example.feldelysium.designsystem.foundation.utils.feldClickable
import com.example.feldelysium.theme.DiscoDialogueTheme

@Composable
fun FeldSettingRow(
    icon: FeldIconId,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
    stampText: String? = null,
    showChevron: Boolean = false,
    role: Role = Role.Button,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
    trailing: (@Composable () -> Unit)? = null,
) {
    val clickable = if (enabled && onClick != null) {
        Modifier.feldClickable(role = role, onClick = onClick)
    } else {
        Modifier
    }

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
            FeldIcon(icon)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = FeldPalette.Muted,
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!enabled) {
                FeldStamp(stampText)
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
    icon: FeldIconId,
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
    stampText: String? = null,
    onClick: (() -> Unit)? = null,
) {
    FeldSettingRow(
        icon = icon,
        title = title,
        subtitle = subtitle,
        modifier = modifier,
        enabled = enabled,
        stampText = stampText,
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
    icon: FeldIconId,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
    stampText: String? = null,
    seed: Int = rememberSeed(),
) {
    FeldSettingRow(
        icon = icon,
        title = title,
        subtitle = subtitle,
        modifier = modifier,
        enabled = enabled,
        stampText = stampText,
        role = Role.Switch,
        onClick = { onCheckedChange(!checked) },
        trailing = {
            FeldSwitch(
                checked = checked,
                onCheckedChange = { },
                seed = seed,
            )
        },
    )
}

@Composable
private fun FeldStamp(stampText: String? = null) {
    Text(
        text = stampText ?: "SOON",
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
                icon = FeldIconId.Cinema,
                title = "Cinema frame",
                subtitle = "Keep playback inside 16:9",
                checked = true,
                onCheckedChange = {},
            )
            FeldSettingValueRow(
                icon = FeldIconId.Cinema,
                title = "Typeface",
                value = "Sina Nova Regular",
                onClick = {},
            )
            FeldSettingRow(
                icon = FeldIconId.Cinema,
                title = "Autoplay",
                subtitle = "Play scene automatically",
                enabled = false,
            )
        }
    }
}
