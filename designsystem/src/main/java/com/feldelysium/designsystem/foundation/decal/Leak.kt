package com.feldelysium.designsystem.foundation.decal

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

internal class Leak(size: Size, rightSide: Boolean, color: Color, strength: Float) {
    private val brush: Brush = run {
        val w = size.width
        val a = strength
        val stops = arrayOf(
            0f to color.copy(alpha = a),
            0.35f to color.copy(alpha = a * 0.45f),
            0.7f to color.copy(alpha = a * 0.1f),
            1f to Color.Transparent,
        )
        if (rightSide) {
            Brush.horizontalGradient(colorStops = stops, startX = w, endX = w * 0.7f)
        } else {
            Brush.horizontalGradient(colorStops = stops, startX = 0f, endX = w * 0.3f)
        }
    }

    fun DrawScope.draw() = drawRect(brush)
}
