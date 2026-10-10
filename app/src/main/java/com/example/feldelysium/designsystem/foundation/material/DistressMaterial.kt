package com.example.feldelysium.designsystem.foundation.material

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.feldelysium.designsystem.foundation.material.agsl.DISTRESS_AGSL

@Immutable
data class DistressSpec(
    val color: Color = Color.Unspecified,
    val strength: Float = 1f,

    val edgeSoftness: Dp = 0.6.dp,
    val edgeRough: Dp = 0.35.dp,
    val edgeSize: Dp = 2.dp,
    val edgeErode: Float = 0.45f,
    val bleed: Float = 0.12f,

    val patchy: Float = 0.2f,
    val patchSize: Dp = 38.dp,

    val dropout: Float = 0.7f,
    val dropoutSize: Dp = 8.dp,

    val scratch: Float = 0.8f,
    val scratchLength: Dp = 60.dp,
    val scratchWidth: Dp = 0.5.dp,
    val scratchAngle: Float = -14f,

    val speck: Float = 0.6f,
    val speckSize: Dp = 3.dp,
    val grain: Float = 0.35f,
    val grainSize: Dp = 0.7.dp,

    val splatter: Float = 0.3f,
    val splatterRadius: Dp = 2.dp,
) {
    fun scaled(amount: Float = 1f, size: Float = 1f, strength: Float = this.strength) = copy(
        strength = strength,
        dropout = dropout * amount,
        scratch = scratch * amount,
        speck = speck * amount,
        grain = grain * amount,
        edgeSize = edgeSize * size,
        dropoutSize = dropoutSize * size,
        speckSize = speckSize * size,
        scratchLength = scratchLength * size,
        grainSize = grainSize * size,
    )
}

fun Modifier.distressMaterial(spec: DistressSpec, seed: Int = 0): Modifier =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) distressShader(spec, seed)
    else this

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun Modifier.distressShader(spec: DistressSpec, seed: Int): Modifier = composed {
    val shader = remember { RuntimeShader(DISTRESS_AGSL) }
    Modifier.graphicsLayer {
        if (size.width <= 0f || size.height <= 0f) return@graphicsLayer
        shader.apply {
            setFloatUniform("seed", seed.mod(97).toFloat())
            setFloatUniform("strength", spec.strength)
            setFloatUniform("edgeSoft", spec.edgeSoftness.toPx())
            setFloatUniform("edgeRough", spec.edgeRough.toPx())
            setFloatUniform("edgeScale", spec.edgeSize.toPx())
            setFloatUniform("edgeErode", spec.edgeErode)
            setFloatUniform("bleed", spec.bleed)
            setFloatUniform("patchy", spec.patchy)
            setFloatUniform("patchScale", spec.patchSize.toPx())
            setFloatUniform("dropout", spec.dropout)
            setFloatUniform("dropoutScale", spec.dropoutSize.toPx())
            setFloatUniform("scratch", spec.scratch)
            setFloatUniform("scratchLen", spec.scratchLength.toPx())
            setFloatUniform("scratchWid", spec.scratchWidth.toPx())
            setFloatUniform("scratchAngle", Math.toRadians(spec.scratchAngle.toDouble()).toFloat())
            setFloatUniform("speck", spec.speck)
            setFloatUniform("speckScale", spec.speckSize.toPx())
            setFloatUniform("grain", spec.grain)
            setFloatUniform("grainScale", spec.grainSize.toPx())
            setFloatUniform("splatter", spec.splatter)
            setFloatUniform("splatterRadius", spec.splatterRadius.toPx())
            val hasColor = spec.color != Color.Unspecified
            setFloatUniform("useInk", if (hasColor) 1f else 0f)
            setColorUniform("inkColor", (if (hasColor) spec.color else Color.White).toArgb())
        }
        renderEffect = RenderEffect.createRuntimeShaderEffect(shader, "content")
            .asComposeRenderEffect()
    }
}
