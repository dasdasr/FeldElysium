package com.feldelysium.designsystem.components.content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.feldelysium.designsystem.foundation.material.DryPaperSpec
import com.feldelysium.designsystem.foundation.material.dryPaperMaterial
import com.feldelysium.designsystem.foundation.seed.rememberSeed

private fun diamondSpec(color: Color) = DryPaperSpec(
    base = color,
    crack = lerp(color, Color.Black, 0.6f),
    tearDepth = 1.4.dp,
    tearScale = 6.dp,
    tearCoverage = 0.35f,
    bow = 0.6.dp,
    crackScale = 7.dp,
    crackDepth = 6.dp,
    crackWidth = 0.8.dp,
    crackStrength = 0.9f,
    crackCoverage = 0.45f,
    grainStrength = 0.07f,
    fiberStrength = 0.06f,
    mottleScale = 14.dp,
    mottleStrength = 0.22f,
)

@Composable
public fun FeldSpeakerDiamond(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    seed: Int = rememberSeed(),
) {
    val spec = remember(color) { diamondSpec(color) }
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(size * 0.7f)
                .rotate(45f)
                .dryPaperMaterial(spec, seed),
        )
    }
}
