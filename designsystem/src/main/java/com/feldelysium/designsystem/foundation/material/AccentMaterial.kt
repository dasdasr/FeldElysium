package com.feldelysium.designsystem.foundation.material

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import com.feldelysium.designsystem.foundation.material.agsl.ACCENT_AGSL
import com.feldelysium.designsystem.foundation.material.agsl.WORN_AGSL

public fun accentBrush(
    color: Color,
    seed: Int,
    density: Float,
    fade: Float = 0.9f,
    glow: Float = 0.9f,
    blotch: Float = 0f,
    grain: Float = 0.07f,
): Brush {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return SolidColor(color)
    val scale = 22f * density
    val grainScale = 0.9f * density
    val shader = RuntimeShader(ACCENT_AGSL).apply {
        setFloatUniform("seed", seed.mod(97).toFloat())
        setFloatUniform("invScale", 1f / scale)
        setFloatUniform("invScale2", 1f / (scale * 0.4f))
        setFloatUniform("invGrain", 1f / grainScale)
        setFloatUniform("invGrain3", 1f / (grainScale * 3f))
        setFloatUniform("fade", fade)
        setFloatUniform("glow", glow)
        setFloatUniform("blotch", blotch)
        setFloatUniform("grain", grain)
        setColorUniform("baseColor", color.toArgb())
    }
    return ShaderBrush(shader)
}

public fun wornBrush(color: Color, seed: Int, density: Float, wear: Float = 0.5f, scale: Float = 26f): Brush {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return SolidColor(color)
    val s = scale * density
    val shader = RuntimeShader(WORN_AGSL).apply {
        setFloatUniform("seed", seed.mod(97).toFloat())
        setFloatUniform("invScale", 1f / s)
        setFloatUniform("invMid1", 1f / (s * 0.35f))
        setFloatUniform("invMid2", 1f / (s * 0.18f))
        setFloatUniform("invFine", 1f / (s * 0.06f))
        setFloatUniform("wear", wear)
        setColorUniform("baseColor", color.toArgb())
    }
    return ShaderBrush(shader)
}
