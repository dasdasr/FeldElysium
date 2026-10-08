package com.example.feldelysium.designsystem.components.input

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feldelysium.designsystem.components.feldClickable
import com.example.feldelysium.designsystem.material.FeldPaper
import com.example.feldelysium.designsystem.material.paperMaterial
import com.example.feldelysium.designsystem.shape.RoughShape
import com.example.feldelysium.designsystem.shape.RoughStyle

private val TrackRough = RoughStyle(
    roughness = 0.7.dp,
    step = 40.dp,
    corner = 20.dp,
    cornerVariation = 0.1f,
    tornChance = 0f,
    jerkChance = 0.05f,
)

private val PillRough = RoughStyle(
    roughness = 0.9.dp,
    step = 30.dp,
    corner = 16.dp,
    cornerVariation = 0.2f,
    tornChance = 0.25f,
)

private val OnPill = Color(0xFF1B1612)
private val OffPill = Color(0xFFE6DCC4)

@Composable
fun FeldSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    seed: Int = 0,
) {
    val trackShape = remember(seed) { RoughShape(seed, TrackRough) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .paperMaterial(trackShape, FeldPaper.Field)
            .padding(3.dp),
    ) {
        options.forEachIndexed { i, label ->
            val selected = i == selectedIndex
            val pillShape = remember(seed, i) { RoughShape(seed + i * 7 + 1, PillRough) }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .then(
                        if (selected) Modifier.paperMaterial(pillShape, FeldPaper.DryButtonCream)
                        else Modifier
                    )
                    .feldClickable(role = Role.Tab, onClick = { onSelect(i) }),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.6.sp),
                    color = if (selected) OnPill else OffPill,
                )
            }
        }
    }
}
