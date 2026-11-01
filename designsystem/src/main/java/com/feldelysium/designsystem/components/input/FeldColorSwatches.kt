package com.feldelysium.designsystem.components.input

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.feldelysium.designsystem.components.container.FeldHandDiamond
import com.feldelysium.designsystem.components.container.entryRough
import com.feldelysium.designsystem.foundation.material.FeldPaper
import com.feldelysium.designsystem.foundation.material.PaperSpec
import com.feldelysium.designsystem.foundation.material.paperMaterial
import com.feldelysium.designsystem.foundation.seed.rememberSeed
import com.feldelysium.designsystem.foundation.shape.RoughShape
import com.feldelysium.designsystem.foundation.sound.feldSounds

private val Cream = Color(0xFFE6DCC4)
private val InkDark = Color(0xFF1B1612)

public fun swatchSpec(argb: Long): PaperSpec {
    val c = Color(argb.toInt())
    return FeldPaper.DryButtonCream.copy(
        base = c,
        scuff = lerp(c, Color.Black, 0.35f),
        ink = Cream,
        inkWidth = 0.dp,

        wear = 0.4f,
        edgeWear = 0.2f,
        innerWear = 0.3f,
        scuffTint = 0.35f,
        chipSize = 5.dp,
        chipCoverage = 0.5f,

        mottleStrength = 0.3f,
        grainStrength = 0.03f,
        toothStrength = 0.03f,
        speckleStrength = 0.03f,
    )
}

@Composable
public fun FeldColorSwatches(
    colors: List<Long>,
    selected: Long,
    onSelect: (Long) -> Unit,
    modifier: Modifier = Modifier,
    seed: Int = rememberSeed(),
    columns: Int = colors.size.coerceAtLeast(1),
    horizontalSpacing: Dp = 5.dp,
    verticalSpacing: Dp = 5.dp,
    minRows: Int = 1,
    diamondFraction: Float = 0.3f,
) {
    val cols = columns.coerceAtLeast(1)
    val rows = colors.chunked(cols)
    val totalRows = maxOf(rows.size, minRows)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
    ) {
        repeat(totalRows) { r ->
            val rowColors = rows.getOrNull(r).orEmpty()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
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
                        Swatch(
                            argb = argb,
                            selected = argb == selected,
                            seed = seed + (r * cols + c) * 5,
                            onClick = { onSelect(argb) },
                            diamondFraction = diamondFraction,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Swatch(
    argb: Long,
    selected: Boolean,
    seed: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    diamondFraction: Float = 0.3f,
) {
    val shape = remember(seed) {
        RoughShape(
            seed = seed,
            style = entryRough(seed).copy(
                corner = 4.dp,
                cornerJitter = 0.15f,
                tornChance = 0.03f,
                forceTorn = -1,
            ),
        )
    }
    val spec = remember(argb) { swatchSpec(argb) }
    val color = Color(argb.toInt())
    val t by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "swatch",
    )

    var swatchSize by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    val sounds = feldSounds

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .onSizeChanged {
                swatchSize = with(density) { it.width.toDp() }
            }
            .graphicsLayer {
                val s = 1f + 0.25f * t
                scaleX = s
                scaleY = s
                rotationZ = -3f * t
            }
            .paperMaterial(shape, spec)
            .feldClickable(
                role = Role.RadioButton,
                onClick = {
                    if (!selected) sounds.select()
                    onClick()
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (t > 0.01f) {
            FeldHandDiamond(
                color = if (color.luminance() > 0.55f) InkDark else Cream,
                seed = seed,
                size = (swatchSize * diamondFraction.coerceIn(0f, 1f))
                    .coerceAtLeast(0.dp),
                modifier = Modifier.graphicsLayer {
                    alpha = t
                },
            )
        }
    }
}
