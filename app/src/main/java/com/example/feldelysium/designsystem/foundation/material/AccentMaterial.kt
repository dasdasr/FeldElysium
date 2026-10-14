package com.example.feldelysium.designsystem.foundation.material

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import com.example.feldelysium.designsystem.foundation.material.agsl.ACCENT_AGSL
import com.example.feldelysium.designsystem.foundation.material.agsl.WORN_AGSL

fun accentBrush(
    color: Color,
    seed: Int,
    density: Float,
    fade: Float = 0.9f,
    glow: Float = 0.9f,
    blotch: Float = 0f,
    grain: Float = 0.07f,
): Brush {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return SolidColor(color)
    val shader = RuntimeShader(ACCENT_AGSL).apply {
        setFloatUniform("seed", seed.mod(97).toFloat())
        setFloatUniform("scale", 22f * density)
        setFloatUniform("grainScale", 0.9f * density)
        setFloatUniform("fade", fade)
        setFloatUniform("glow", glow)
        setFloatUniform("blotch", blotch)
        setFloatUniform("grain", grain)
        setColorUniform("baseColor", color.toArgb())
    }
    return ShaderBrush(shader)
}

fun wornBrush(color: Color, seed: Int, density: Float, wear: Float = 0.5f, scale: Float = 26f): Brush {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return SolidColor(color)
    val shader = RuntimeShader(WORN_AGSL).apply {
        setFloatUniform("seed", seed.mod(97).toFloat())
        setFloatUniform("scale", scale * density)
        setFloatUniform("wear", wear)
        setColorUniform("baseColor", color.toArgb())
    }
    return ShaderBrush(shader)
}
