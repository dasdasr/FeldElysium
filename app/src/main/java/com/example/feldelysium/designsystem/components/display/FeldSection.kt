package com.example.feldelysium.designsystem.components.display

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feldelysium.designsystem.components.settings.FeldSettingValueRow
import com.example.feldelysium.designsystem.components.title.FeldTitleTag
import com.example.feldelysium.designsystem.foundation.utils.FeldPalette
import com.example.feldelysium.designsystem.foundation.utils.feldClickable
import com.example.feldelysium.theme.DiscoDialogueTheme

@Composable
fun FeldSection(
    title: String,
    modifier: Modifier = Modifier,
    count: String? = null,
    expanded: Boolean? = null,
    onToggle: (() -> Unit)? = null,
    distressedTag: Boolean = true,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    Column(
        modifier
            .fillMaxWidth()
            .animateContentSize(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (onToggle != null) Modifier.feldClickable(onClick = onToggle) else Modifier),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FeldTitleTag(
                text = title,
                distressed = distressedTag,
                textStyle = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                ),
            )
            Spacer(Modifier.weight(1f))
            count?.let {
                Text(it, style = MaterialTheme.typography.labelSmall, color = FeldPalette.Muted)
            }
            if (expanded != null) {
                FeldIcon(
                    FeldIconId.Chevron,
                    size = 18.dp,
                    modifier = Modifier
                        .padding(start = 8.dp, end = 4.dp)
                        .rotate(if (expanded) -90f else 90f),
                )
            }
        }
        FeldDivider()
        AnimatedVisibility(visible = expanded != false) {
            Column(content = content)
        }
    }
}

@Preview(widthDp = 380, heightDp = 360, showBackground = true, backgroundColor = 0xFF0B1011)
@Composable
private fun FeldSectionPreview() {
    DiscoDialogueTheme {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            FeldSection("Skills", count = "5", expanded = true, onToggle = {}) {
                FeldSettingValueRow(
                    icon = FeldIconId.Aspect,
                    title = "Logic",
                    value = "Reason. Question. Understand.",
                    onClick = {},
                )
                FeldSettingValueRow(
                    icon = FeldIconId.Aspect,
                    title = "Empathy",
                    value = "Feel. Connect. Care.",
                    onClick = {},
                )
            }

            FeldSection("NPCs", count = "4", expanded = false, onToggle = {})
            FeldSection("Format")
        }
    }
}
