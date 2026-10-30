package com.feldelysium.designsystem.components.button

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
import com.feldelysium.designsystem.components.content.FeldDiamond
import com.feldelysium.designsystem.components.content.FeldIcon
import com.feldelysium.designsystem.components.content.FeldIconId
import com.feldelysium.designsystem.components.input.feldClickable
import com.feldelysium.designsystem.foundation.material.FeldMarker
import com.feldelysium.designsystem.foundation.material.MarkerFilmSpec
import com.feldelysium.designsystem.theme.FeldTheme

@Composable
public fun FeldIconButton(
    id: FeldIconId,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    enabled: Boolean = true,
    markerFilmSpec: MarkerFilmSpec = FeldMarker.Chalk,
) {
    Box(
        modifier
            .sizeIn(minWidth = 40.dp, minHeight = 40.dp)
            .feldClickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { FeldIcon(id, markerFilmSpec = markerFilmSpec, size = size) }
}

@Preview(widthDp = 400, heightDp = 260, showBackground = true, backgroundColor = 0xFF0B1011)
@Composable
private fun FeldIconPreview() {
    FeldTheme {
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
