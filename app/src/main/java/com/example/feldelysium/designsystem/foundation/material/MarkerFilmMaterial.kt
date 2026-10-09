package com.example.feldelysium.designsystem.foundation.material

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.feldelysium.designsystem.foundation.material.agsl.MARKER_FILM_AGSL

@Immutable
data class MarkerFilmSpec(
    val color: Color,
    val streakLen: Dp = 6.dp,
    val streakWidth: Dp = 1.2.dp,
    val dryness: Float = 0.3f,
    val gapStrength: Float = 0.85f,
    val smudgeScale: Dp = 22.dp,
    val smudgeStrength: Float = 0.25f,
    val poreScale: Dp = 1.1.dp,
    val poreDensity: Float = 0.07f,
    val grainScale: Dp = 0.6.dp,
    val grainStrength: Float = 0.25f,
)

object FeldMarker {
    val Chalk = MarkerFilmSpec(color = Color(0xFFE6E3DA))
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun MarkerFilmSpec.toShader(seed: Int, px: (Dp) -> Float) =
    RuntimeShader(MARKER_FILM_AGSL).apply {
        setFloatUniform("seed", seed.mod(97).toFloat())
        setFloatUniform("streakLen", px(streakLen))
        setFloatUniform("streakWidth", px(streakWidth))
        setFloatUniform("dryness", dryness)
        setFloatUniform("gapStrength", gapStrength)
        setFloatUniform("smudgeScale", px(smudgeScale))
        setFloatUniform("smudgeStrength", smudgeStrength)
        setFloatUniform("poreScale", px(poreScale))
        setFloatUniform("poreDensity", poreDensity)
        setFloatUniform("grainScale", px(grainScale))
        setFloatUniform("grainStrength", grainStrength)
        setColorUniform("baseColor", color.toArgb())
    }

fun markerFilmBrush(spec: MarkerFilmSpec, seed: Int, density: Float): Brush {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return SolidColor(spec.color)
    return ShaderBrush(spec.toShader(seed) { it.value * density })
}

fun Modifier.markerFilmMaterial(spec: MarkerFilmSpec, seed: Int = 0): Modifier =
    graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithCache {
            val brush: Brush =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    ShaderBrush(spec.toShader(seed) { it.toPx() })
                else SolidColor(spec.color)
            onDrawWithContent {
                drawContent()
                drawRect(brush, blendMode = BlendMode.SrcIn)
            }
        }
