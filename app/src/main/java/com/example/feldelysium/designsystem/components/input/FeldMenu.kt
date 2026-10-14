package com.example.feldelysium.designsystem.components.input

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.feldelysium.designsystem.components.card.FeldHandDiamond
import com.example.feldelysium.designsystem.components.display.FeldDivider
import com.example.feldelysium.designsystem.foundation.material.FeldPaper
import com.example.feldelysium.designsystem.foundation.material.paperMaterial
import com.example.feldelysium.designsystem.foundation.shape.RoughShape
import com.example.feldelysium.designsystem.foundation.shape.RoughStyle
import com.example.feldelysium.designsystem.foundation.utils.FeldPalette
import com.example.feldelysium.designsystem.foundation.utils.feldClickable

data class FeldMenuItem(val label: String, val destructive: Boolean = false, val onClick: () -> Unit)

private val MenuRough = RoughStyle(
    roughness = 0.6.dp,
    step = 40.dp,
    corner = 3.dp,
    cornerVariation = 0.2f,
    cornerJitter = 0.2f,
    tornChance = 0.03f,
    jerkChance = 0.04f,
)

@Composable
fun FeldMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    items: List<FeldMenuItem>,
    modifier: Modifier = Modifier,
    seed: Int = 11,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
) {
    val shape = remember(seed) { RoughShape(seed, MenuRough) }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        offset = offset,
        containerColor = Color.Transparent,
        shape = RectangleShape,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = modifier,
    ) {
        Column(
            Modifier
                .width(IntrinsicSize.Max)
                .widthIn(min = 168.dp)
                .paperMaterial(shape, FeldPaper.Field)
                .padding(vertical = 4.dp),
        ) {
            items.forEachIndexed { i, item ->
                if (i > 0) FeldDivider(Modifier.padding(horizontal = 12.dp))
                val color = if (item.destructive) {
                    FeldPalette.Orange
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .feldClickable(onClick = {
                            onDismiss()
                            item.onClick()
                        })
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FeldHandDiamond(color, seed + i, 9.dp)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = item.label.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = color,
                    )
                }
            }
        }
    }
}
