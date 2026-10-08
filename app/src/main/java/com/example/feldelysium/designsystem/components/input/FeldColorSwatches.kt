package com.example.feldelysium.designsystem.components.input

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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

private val CleanRound = RoughStyle(
    roughness = 0.dp,
    irregularity = 0f,
    slide = 0f,
    corner = 9.dp,
    cornerVariation = 0f,
    cornerJitter = 0f,
    inset = 0.dp,
    jerkChance = 0f,
    tornChance = 0f,
)

private val Cream = Color(0xFFE6DCC4)

fun swatchSpec(argb: Long, selected: Boolean = false): PaperSpec {
    val c = Color(argb.toInt())
    return FeldPaper.DryButtonCream.copy(
        base = c,
        scuff = lerp(c, Color.Black, 0.35f),
        ink = Cream,
        inkWidth = if (selected) 2.dp else 0.dp,

        wear = 0.5f,
        edgeWear = 0f,
        innerWear = 0.5f,
        scuffTint = 0.45f,
        chipSize = 5.dp,
        chipCoverage = 0.5f,

        mottleStrength = 0.5f,
        grainStrength = 0.06f,
        toothStrength = 0.05f,
        speckleStrength = 0.05f,
    )
}

@Composable
fun FeldColorSwatches(
    colors: List<Long>,
    selected: Long,
    onSelect: (Long) -> Unit,
    modifier: Modifier = Modifier,
    seed: Int = 0,
    columns: Int = colors.size.coerceAtLeast(1),
    minRows: Int = 1,
) {
    val cols = columns.coerceAtLeast(1)
    val rows = colors.chunked(cols)
    val totalRows = maxOf(rows.size, minRows)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        repeat(totalRows) { r ->
            val rowColors = rows.getOrNull(r).orEmpty()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                repeat(cols) { c ->
                    val argb = rowColors.getOrNull(c)
                    if (argb == null) {
                        Spacer(
                            Modifier
                                .weight(1f)
                                .aspectRatio(1f),
                        )
                    } else {
                        val shape = remember(seed, r, c) {
                            RoughShape(seed + (r * cols + c) * 5, CleanRound)
                        }
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
        }
    }
}
