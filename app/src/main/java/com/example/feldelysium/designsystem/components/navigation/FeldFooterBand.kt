package com.example.feldelysium.designsystem.components.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.feldelysium.designsystem.components.content.FeldDiamond

// TODO: CHange to torn texture
@Composable
fun FeldFooterBand(sceneName: String, modifier: Modifier = Modifier) {
    val ink = Color(0xFF1B1612)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFD9C5A1))
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "SCENE",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFE6DCC4),
                modifier = Modifier.background(ink).padding(horizontal = 8.dp, vertical = 8.dp),
            )
            Text(
                text = sceneName.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .border(1.dp, ink)
                    .padding(horizontal = 12.dp, vertical = 7.dp),
            )
        }
        Row(
            modifier = Modifier.padding(start = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            repeat(3) { FeldDiamond(color = ink, size = 14.dp) }
        }
    }
}
