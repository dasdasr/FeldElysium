package com.feldelysium.designsystem.foundation.decal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
internal class DecalLayer internal constructor(
    private val ambient: Float,
    private val seed: Int,
    private val spec: DecalSpec,
    private val context: DecalContext,
    private val leftInset: Dp,
    private val measurer: TextMeasurer,
    private val style: TextStyle,
) {
    internal fun plan(size: Size, density: Density): DecalPlan = planDecals(
        seed,
        size,
        ambient,
        spec,
        context,
        density,
        measurer,
        with(density) { leftInset.toPx() },
    )
}

@Composable
internal fun rememberDecalLayer(
    ambient: Float?,
    seed: Int,
    spec: DecalSpec,
    context: DecalContext,
    leftInset: Dp = 0.dp,
    style: TextStyle = FeldDecalType.Base,
): DecalLayer? {
    val measurer = rememberTextMeasurer(cacheSize = 64)
    return remember(ambient, seed, spec, context, leftInset, style, measurer) {
        if (ambient == null || ambient <= 0f) {
            null
        } else {
            DecalLayer(ambient, seed, spec, context, leftInset, measurer, style)
        }
    }
}
