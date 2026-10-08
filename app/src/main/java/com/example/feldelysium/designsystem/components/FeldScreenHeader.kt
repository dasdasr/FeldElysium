package com.example.feldelysium.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feldelysium.theme.DiscoDialogueTheme

@Composable
fun FeldScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    subtitle: String? = null,
    metaStart: String? = null,
    metaEnd: String? = null,
    navIcon: FeldIconId? = FeldIconId.Back,
    onNav: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    seed: Int = 0,
    titleSpec: DistressedTitleSpec = FeldDistressedTitles.Large,
    titleStyle: TextStyle = MaterialTheme.typography.titleLarge.copy(
        fontSize = 52.sp,
        lineHeight = 54.sp,
        letterSpacing = 1.sp,
    ),
    divider: Boolean = true,
) {
    Column(modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (navIcon != null) {
                FeldIconButton(navIcon, onNav, tint = FeldPalette.Muted, modifier = Modifier.padding(start = 0.dp))
            }
            if (eyebrow != null) {
                FeldDiamond(FeldPalette.Orange, size = 10.dp)
                Text(
                    text = eyebrow.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = FeldPalette.Muted,
                    modifier = Modifier.padding(start = 10.dp),
                )
            }
            Spacer(Modifier.weight(1f))
            actions()
        }

        Spacer(Modifier.height(6.dp))
        FeldDistressedTitle(
            text = title,
            seed = seed,
            spec = titleSpec,
            textStyle = titleStyle,
        )

        if (subtitle != null) {
            Text(
                text = subtitle.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = FeldPalette.Muted,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        if (metaStart != null || metaEnd != null) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                metaStart?.let {
                    Text(it.uppercase(), style = MaterialTheme.typography.labelSmall, color = FeldPalette.Muted)
                }
                FeldDivider(
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                    seed = seed + 5,
                )
                metaEnd?.let {
                    Text(it.uppercase(), style = MaterialTheme.typography.labelSmall, color = FeldPalette.Muted)
                }
            }
        }

        if (divider) {
            Spacer(Modifier.height(12.dp))
            FeldDivider(seed = seed + 9, thickness = 1.dp)
        }
    }
}

@Preview(widthDp = 380, heightDp = 360, showBackground = true, backgroundColor = 0xFF0B1011)
@Composable
private fun FeldScreenHeaderPreview() {
    DiscoDialogueTheme {
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            FeldScreenHeader(
                title = "The final interview",
                eyebrow = "Scene editor",
                metaStart = "4 / 12",
                metaEnd = "Dialogue scene",
                seed = 2,
                actions = { FeldIconButton(FeldIconId.More, onClick = {}) },
            )
            FeldScreenHeader(
                title = "Choose speaker",
                eyebrow = "Choose speaker",
                subtitle = "Select who will say this line.",
                navIcon = FeldIconId.Close,
                seed = 7,
            )
        }
    }
}
