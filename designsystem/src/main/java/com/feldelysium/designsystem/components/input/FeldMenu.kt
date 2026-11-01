package com.feldelysium.designsystem.components.input

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.sp
import com.feldelysium.designsystem.components.container.FeldDivider
import com.feldelysium.designsystem.components.content.FeldIcon
import com.feldelysium.designsystem.components.content.FeldIconId
import com.feldelysium.designsystem.foundation.material.FeldPaper
import com.feldelysium.designsystem.foundation.material.paperMaterial
import com.feldelysium.designsystem.foundation.shape.RoughShape
import com.feldelysium.designsystem.foundation.shape.RoughStyle
import com.feldelysium.designsystem.theme.FeldPalette

public data class FeldMenuItem(
    val label: String,
    val icon: FeldIconId? = null,
    val destructive: Boolean = false,
    val onClick: () -> Unit,
)

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
public fun FeldMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    items: List<FeldMenuItem>,
    modifier: Modifier = Modifier,
    seed: Int = 11,
    offset: DpOffset = DpOffset(12.dp, 0.dp),
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
                .paperMaterial(shape, FeldPaper.Field)
                .padding(vertical = 4.dp),
        ) {
            items.forEachIndexed { i, item ->
                if (i > 0) FeldDivider(Modifier.padding(horizontal = 14.dp))
                MenuRow(item, seed + i, onDismiss)
            }
        }
    }
}

@Composable
private fun MenuRow(item: FeldMenuItem, seed: Int, onDismiss: () -> Unit) {
    val color = if (item.destructive) FeldPalette.Orange else MaterialTheme.colorScheme.onSurface
    Row(
        Modifier
            .fillMaxWidth()
            .feldClickable(onClick = {
                onDismiss()
                item.onClick()
            })
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(20.dp).padding(bottom = 2.dp), contentAlignment = Alignment.Center) {
            if (item.icon != null) {
                FeldIcon(id = item.icon, seed = seed, size = 18.dp, film = false)
            }
        }
        Text(
            text = item.label.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.2.sp),
            color = color,
            modifier = Modifier.padding(start = 2.dp),
        )
    }
}
