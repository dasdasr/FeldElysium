package com.example.feldelysium.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.feldelysium.theme.DiscoDialogueTheme

@Composable
fun FeldSettingRow(
    title: String,
    modifier: Modifier = Modifier,
    icon: FeldIconId? = null,
    value: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    large: Boolean = false,
    divider: Boolean = true,
    seed: Int = 0,
) {
    Column(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.feldClickable(onClick = onClick) else Modifier)
                .padding(horizontal = 4.dp, vertical = if (large) 14.dp else 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (icon != null) {
                FeldIcon(icon, tint = if (large) colorScheme.onSurface else FeldPalette.Muted, size = 24.dp)
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = title.uppercase(),
                    style = if (large) MaterialTheme.typography.labelLarge
                    else MaterialTheme.typography.labelSmall,
                    color = if (large) colorScheme.onSurface else FeldPalette.Muted,
                )
                if (value != null) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurface,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
            when {
                trailing != null -> trailing()
                onClick != null -> FeldIcon(FeldIconId.Chevron, tint = FeldPalette.Muted, size = 20.dp)
            }
        }
        if (divider) FeldDivider(seed = seed, color = FeldPalette.Line.copy(alpha = 0.7f))
    }
}

@Preview(widthDp = 380, heightDp = 420, showBackground = true, backgroundColor = 0xFF0B1011)
@Composable
private fun FeldSettingRowPreview() {
    DiscoDialogueTheme {
        var fast by remember { mutableStateOf(true) }
        Column(Modifier.padding(16.dp)) {
            FeldSettingRow("Media", icon = FeldIconId.Image, large = true, seed = 1)
            FeldSettingRow("Fit", icon = FeldIconId.Aspect, value = "Cover", onClick = {}, seed = 2)
            FeldSettingRow("Filter", icon = FeldIconId.Palette, value = "None", onClick = {}, seed = 3)
            FeldSettingRow(
                title = "Fast add",
                icon = FeldIconId.FastAdd,
                value = "Add lines without picking a speaker",
                trailing = { FeldSwitch(fast, { fast = it }, seed = 4) },
                seed = 4,
            )
        }
    }
}
