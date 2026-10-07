package com.example.feldelysium.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class FeldToolbarItem(
    val icon: FeldIconId,
    val label: String,
    val enabled: Boolean = true,
    val onClick: () -> Unit,
)

@Composable
fun FeldToolbar(
    items: List<FeldToolbarItem>,
    modifier: Modifier = Modifier,
    seed: Int = 0,
) {
    Column(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            items.forEachIndexed { i, item ->
                if (i > 0) {
                    FeldDivider(
                        vertical = true,
                        seed = seed + i,
                        modifier = Modifier.padding(vertical = 14.dp),
                    )
                }
                val tint = 	colorScheme.onSurface.copy(alpha = if (item.enabled) 0.9f else 0.35f)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = item.enabled, onClick = item.onClick)
                        .padding(vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    FeldIcon(item.icon, tint = tint, size = 24.dp)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = item.label.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = tint,
                    )
                }
            }
        }
        FeldDivider(seed = seed + 10, segment = 160.dp)
    }
}
