@file:Suppress("MatchingDeclarationName")

package com.feldelysium.designsystem.components.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.feldelysium.designsystem.components.content.FeldIcon
import com.feldelysium.designsystem.components.content.FeldIconId
import com.feldelysium.designsystem.components.input.FeldSwitch
import com.feldelysium.designsystem.components.input.feldClickable
import com.feldelysium.designsystem.foundation.seed.rememberSeed
import com.feldelysium.designsystem.theme.FeldPalette
import com.feldelysium.designsystem.theme.FeldTheme

public enum class FeldSettingStatus {
    SOON,
    LOCK,
}

@Composable
public fun FeldSettingRow(
    icon: FeldIconId,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    status: FeldSettingStatus? = null,
    showChevron: Boolean = false,
    role: Role = Role.Button,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
    trailing: (@Composable () -> Unit)? = null,
) {
    val enabled = status == null
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
            if (status != null) {
                FeldStamp(status)
            } else {
                trailing?.invoke()

                if (showChevron) {
                    FeldIcon(
                        id = FeldIconId.Chevron,
                        size = 18.dp,
                        modifier = Modifier.padding(start = 4.dp),
                        seed = title.hashCode(),
                    )
                }
            }
        }
    }
}

@Composable
public fun FeldSettingValueRow(
    icon: FeldIconId,
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    status: FeldSettingStatus? = null,
    onClick: (() -> Unit)? = null,
) {
    FeldSettingRow(
        icon = icon,
        title = title,
        subtitle = subtitle,
        modifier = modifier,
        status = status,
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
public fun FeldSettingSwitchRow(
    icon: FeldIconId,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    status: FeldSettingStatus? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
    seed: Int = rememberSeed(),
) {
    FeldSettingRow(
        icon = icon,
        title = title,
        subtitle = subtitle,
        modifier = modifier,
        status = status,
        role = Role.Switch,
        contentPadding = contentPadding,
        onClick = { onCheckedChange(!checked) },
        trailing = {
            FeldSwitch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = status == null,
                seed = seed,
            )
        },
    )
}

@Composable
private fun FeldStamp(status: FeldSettingStatus) {
    Text(
        text = status.name,
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
    FeldTheme {
        Column {
            FeldSettingSwitchRow(
                icon = FeldIconId.Cinema,
                title = "Cinema frame",
                subtitle = "Keep playback inside 16:9",
                checked = true,
                onCheckedChange = {},
            )
            FeldSettingValueRow(
                icon = FeldIconId.Typeface,
                title = "Typeface",
                value = "Sina Nova Regular",
                onClick = {},
            )
            FeldSettingRow(
                icon = FeldIconId.Autoplay,
                title = "Autoplay",
                subtitle = "Play scene automatically",
                status = FeldSettingStatus.LOCK,
            )
        }
    }
}
