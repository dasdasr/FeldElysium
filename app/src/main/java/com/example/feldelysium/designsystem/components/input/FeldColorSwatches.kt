package com.example.feldelysium.designsystem.components.input

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.feldelysium.designsystem.components.feldClickable
import com.example.feldelysium.designsystem.material.FeldPaper
import com.example.feldelysium.designsystem.material.PaperSpec
import com.example.feldelysium.designsystem.material.paperMaterial
import com.example.feldelysium.designsystem.shape.RoughShape
import com.example.feldelysium.designsystem.shape.RoughStyle

private val SwatchRough = RoughStyle(
    roughness = 0.6.dp,
    step = 20.dp,
    corner = 3.dp,
    cornerVariation = 0.3f,
    tornChance = 0.15f,
)

private val Cream = Color(0xFFE6DCC4)

fun swatchSpec(argb: Long, selected: Boolean = false): PaperSpec {
    val c = Color(argb.toInt())
    return FeldPaper.DryButtonCream.copy(
        base = c,
        scuff = lerp(c, Color.Black, 0.3f),
        ink = Cream,
        inkWidth = if (selected) 2.dp else 0.dp,
    )
}

@Composable
fun FeldColorSwatches(
    colors: List<Long>,
    selected: Long,
    onSelect: (Long) -> Unit,
    modifier: Modifier = Modifier,
    seed: Int = 0,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        colors.forEachIndexed { i, argb ->
            val shape = remember(seed, i) { RoughShape(seed + i * 5, SwatchRough) }
            Box(
                Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .paperMaterial(shape, swatchSpec(argb, selected = argb == selected))
                    .feldClickable(role = Role.RadioButton, onClick = { onSelect(argb) }),
            )
        }
    }
}
