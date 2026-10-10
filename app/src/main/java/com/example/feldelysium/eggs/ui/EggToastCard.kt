package com.example.feldelysium.eggs.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import com.example.feldelysium.designsystem.foundation.material.accentBrush
import kotlin.random.Random

internal fun Modifier.toastCard(seed: Int, accent: Color): Modifier = drawWithCache {
    val d = density
    val w = size.width
    val h = size.height
    val rnd = Random(seed)

    val bgBrush = accentBrush(
        Ink.copy(alpha = 0.94f), seed + 5, d,
        fade = 0f, glow = 0f, blotch = 0.05f, grain = 0.04f,
    )
    val fill = accentBrush(accent, seed, d)

    val body = chamferRectPoints(
        Rect(0f, 0f, w, h),
        cutTL = 3 * d, cutTR = 6 * d, cutBR = 5 * d, cutBL = 3 * d,
        step = 10 * d, jitter = 0.9f * d, rnd = rnd,
    ).toPath()

    class Speck(val at: Offset, val s: Float, val light: Boolean, val a: Float)
    val specks = List(45) {
        Speck(
            Offset(rnd.nextFloat() * w, rnd.nextFloat() * h),
            (0.5f + rnd.nextFloat() * 1.0f) * d,
            rnd.nextFloat() < 0.5f,
            0.03f + rnd.nextFloat() * 0.06f,
        )
    }
    val scratches = List(4) {
        val p = Offset(rnd.nextFloat() * w * 0.5f + 20 * d, rnd.nextFloat() * h)
        p to (p + Offset((40 + rnd.nextFloat() * 90) * d, (rnd.nextFloat() - 0.5f) * 3 * d))
    }
    val hairlineA = Offset(14 * d, 0.7f * d) to Offset(w * 0.42f, 0.9f * d)
    val hairlineB = Offset(w * 0.50f, 0.8f * d) to Offset(w * 0.86f, 0.6f * d)
    val bottomWear = Offset(w * 0.2f, h - 0.8f * d) to
            Offset(w * 0.55f, h - 0.6f * d)

    onDrawBehind {
        drawPath(body, bgBrush)
        specks.forEach {
            drawCircle(
                if (it.light) CreamDim.copy(alpha = it.a) else Color.Black.copy(alpha = it.a * 3f),
                it.s * 0.5f,
                it.at,
            )
        }
        scratches.forEach { (a, b) -> drawLine(Cream.copy(alpha = 0.05f), a, b, 0.8f * d) }
        drawLine(Cream.copy(alpha = 0.35f), hairlineA.first, hairlineA.second, 1f * d)
        drawLine(Cream.copy(alpha = 0.30f), hairlineB.first, hairlineB.second, 1f * d)

        drawLine(
            Cream.copy(alpha = 0.25f),
            bottomWear.first,
            bottomWear.second,
            1f * d,
        )

        listOf(0.26f, 0.40f, 0.54f).forEach { fy ->
            val c = Offset(w - 14 * d, h * fy)
            val r = 3f * d
            drawPath(
                Path().apply {
                    moveTo(c.x, c.y - r)
                    lineTo(c.x + r, c.y)
                    lineTo(c.x, c.y + r)
                    lineTo(c.x - r, c.y)
                    close()
                },
                CreamDim.copy(alpha = 0.55f),
            )
        }

        clipPath(body) {
            drawRect(fill, Offset.Zero, Size(9 * d, h))
            drawRect(fill, Offset(w - 3 * d, 0f), Size(3 * d, h))
        }
        drawPath(body, Color(0xFF2A2D2B), style = Stroke(0.8f * d))
    }
}
