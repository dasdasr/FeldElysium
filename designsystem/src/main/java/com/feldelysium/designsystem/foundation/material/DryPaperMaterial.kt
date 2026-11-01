package com.feldelysium.designsystem.foundation.material

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.feldelysium.designsystem.foundation.material.agsl.DRY_PAPER_AGSL

@Immutable
public data class DryPaperSpec(
    val base: Color,
    val crack: Color,

    val tearDepth: Dp = 1.6.dp,
    val tearScale: Dp = 14.dp,
    val tearCoverage: Float = 0.1f,
    val bow: Dp = 1.2.dp,

    val crackScale: Dp = 18.dp,
    val crackDepth: Dp = 20.dp,
    val crackWidth: Dp = 1.1.dp,
    val crackStrength: Float = 0.8f,
    val crackCoverage: Float = 0.2f,

    val grainScale: Dp = 1.0.dp,
    val grainStrength: Float = 0.05f,
    val fiberStrength: Float = 0.04f,
    val mottleScale: Dp = 34.dp,
    val mottleStrength: Float = 0.14f,
    val pressShade: Float = 0.06f,
)

internal fun Modifier.dryPaperMaterial(spec: DryPaperSpec, seed: Int = 0, press: () -> Float = { 0f }): Modifier =
    drawWithCache {
        val rim = 1.dp.toPx()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val shader = RuntimeShader(DRY_PAPER_AGSL).apply {
                setFloatUniform("size", size.width, size.height)
                setFloatUniform("seed", seed.mod(97).toFloat())
                setFloatUniform("rim", rim)
                setFloatUniform("tearDepth", spec.tearDepth.toPx())
                setFloatUniform("tearScale", spec.tearScale.toPx())
                setFloatUniform("tearCoverage", spec.tearCoverage)
                setFloatUniform("bow", spec.bow.toPx())
                setFloatUniform("crackScale", spec.crackScale.toPx())
                setFloatUniform("crackDepth", spec.crackDepth.toPx())
                setFloatUniform("crackWidth", spec.crackWidth.toPx())
                setFloatUniform("crackStrength", spec.crackStrength)
                setFloatUniform("crackCoverage", spec.crackCoverage)
                setFloatUniform("grainScale", spec.grainScale.toPx())
                setFloatUniform("grainStrength", spec.grainStrength)
                setFloatUniform("fiberStrength", spec.fiberStrength)
                setFloatUniform("mottleScale", spec.mottleScale.toPx())
                setFloatUniform("mottleStrength", spec.mottleStrength)
                setFloatUniform("pressShade", spec.pressShade)
                setColorUniform("baseColor", spec.base.toArgb())
                setColorUniform("crackColor", spec.crack.toArgb())
            }
            val brush = ShaderBrush(shader)
            onDrawWithContent {
                shader.setFloatUniform("press", press())
                drawRect(brush)
                drawContent()
            }
        } else {
            onDrawWithContent {
                drawRect(spec.base)
                drawContent()
            }
        }
    }
