package com.example.feldelysium.designsystem.foundation.decal

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.toArgb

private const val LEAK_AGSL = """
uniform float2 size;
uniform float seed;
uniform float strength;
uniform float side;
layout(color) uniform half4 tint;
half4 main(float2 p) {
    float x = mix(p.x, size.x - p.x, side);
    float u = x / (size.x * 0.24);
    float fall = exp(-u * u * 1.4);
    float y = p.y / size.y;
    float v = 0.6 + 0.4 * sin(y * (3.0 + fract(seed * 0.37) * 4.0) + seed);
    float a = fall * v * strength;
    a += (fract(sin(dot(p, float2(12.9898, 78.233))) * 43758.5453) - 0.5) / 255.0;
    a = clamp(a, 0.0, 1.0);
    return half4(tint.rgb * half(a), half(a));
}
"""

internal class Leak(size: Size, rightSide: Boolean, color: Color, strength: Float, seed: Float) {
    private val brush: Brush =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ShaderBrush(
                RuntimeShader(LEAK_AGSL).apply {
                setFloatUniform("size", size.width.toFloat(), size.height.toFloat())
                setFloatUniform("seed", seed)
                setFloatUniform("strength", strength)
                setFloatUniform("side", if (rightSide) 1f else 0f)
                setColorUniform("tint", color.toArgb())
            }
            )
        } else {
            val c = color.copy(alpha = strength * 0.7f)
            if (rightSide) {
                Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, c),
                    startX = size.width * 0.76f,
                    endX = size.width.toFloat(),
                )
            } else {
                Brush.horizontalGradient(
                    colors = listOf(c, Color.Transparent),
                    startX = 0f,
                    endX = size.width.toFloat() * 0.24f,
                )
            }
        }

    fun DrawScope.draw() = drawRect(brush)
}
