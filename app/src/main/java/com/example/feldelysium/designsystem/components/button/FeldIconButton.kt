package com.example.feldelysium.designsystem.components.button

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.feldelysium.designsystem.components.display.FeldDiamond
import com.example.feldelysium.designsystem.components.display.FeldIcon
import com.example.feldelysium.designsystem.components.display.FeldIconId
import com.example.feldelysium.designsystem.foundation.utils.FeldPalette
import com.example.feldelysium.designsystem.foundation.utils.feldClickable
import com.example.feldelysium.theme.DiscoDialogueTheme

@Composable
fun FeldIconButton(
    id: FeldIconId,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = FeldPalette.Muted,
    size: Dp = 24.dp,
    enabled: Boolean = true,
) {
    Box(
        modifier
            .sizeIn(minWidth = 40.dp, minHeight = 40.dp)
            .feldClickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { FeldIcon(id, size = size) }
}

@Preview(widthDp = 400, heightDp = 260, showBackground = true, backgroundColor = 0xFF0B1011)
@Composable
private fun FeldIconPreview() {
    DiscoDialogueTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                FeldIconId.entries.forEach { FeldIcon(it) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FeldDiamond(Color(0xFF4FB3A6))
                FeldDiamond(Color(0xFFE45720))
                FeldDiamond(Color(0xFF8F6FD6), outline = true)
                FeldDiamond(Color(0xFFD9B23A), size = 14.dp)
            }
        }
    }
}
