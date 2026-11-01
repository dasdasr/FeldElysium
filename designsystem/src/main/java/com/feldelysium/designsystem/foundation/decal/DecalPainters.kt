package com.feldelysium.designsystem.foundation.decal

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.drawText

internal interface Decal {
    val extent: Size
    fun DrawScope.draw(color: Color, alpha: Float)
}

internal class Placement(
    val decal: Decal,
    val bounds: Rect,
    val rotation: Float,
    val mirror: Boolean,
    val color: Color,
    val alpha: Float,
)

internal class RulerDecal(
    private val length: Float,
    private val height: Float,
    private val step: Float,
    private val longEvery: Int,
    private val stroke: Float,
) : Decal {
    override val extent = Size(length, height)
    override fun DrawScope.draw(color: Color, alpha: Float) {
        val c = color.copy(alpha = alpha)
        drawLine(c, Offset(0f, height), Offset(length, height), stroke)
        var i = 0
        var x = 0f
        while (x <= length) {
            val h = if (i % longEvery == 0) height * 0.95f else height * 0.4f
            drawLine(c, Offset(x, height), Offset(x, height - h), stroke)
            x += step
            i++
        }
    }
}

internal class StripDecal(
    private val length: Float,
    private val thick: Float,
    private val hole: Float,
    private val pitch: Float,
    private val text: TextLayoutResult?,
    val vertical: Boolean,
) : Decal {
    override val extent = Size(length, thick)
    override fun DrawScope.draw(color: Color, alpha: Float) {
        val c = color.copy(alpha = alpha)
        val hw = hole * 1.5f
        val r = CornerRadius(hole * 0.3f)
        var x = pitch * 0.25f
        while (x + hw <= length) {
            drawRoundRect(c, Offset(x, 0f), Size(hw, hole), r)
            if (text != null) drawRoundRect(c, Offset(x, thick - hole), Size(hw, hole), r)
            x += pitch
        }
        text?.let {
            drawText(it, color = c, topLeft = Offset(pitch * 0.5f, hole + (thick - 2 * hole - it.size.height) / 2f))
        }
    }
}

internal class TextDecal(private val layout: TextLayoutResult) : Decal {
    override val extent = Size(layout.size.width.toFloat(), layout.size.height.toFloat())
    override fun DrawScope.draw(color: Color, alpha: Float) {
        drawText(layout, color = color.copy(alpha = alpha))
    }
}

internal enum class GlyphShape { Diamond, DiamondFilled, Triangle, TriangleDown, Play, Cross, Register }

internal class GlyphDecal(private val shape: GlyphShape, private val side: Float, private val stroke: Float) : Decal {
    override val extent = Size(side, side)
    override fun DrawScope.draw(color: Color, alpha: Float) {
        val c = color.copy(alpha = alpha)
        val s = side
        val line = Stroke(stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
        fun poly(pts: List<Offset>, fill: Boolean) {
            val p = Path().apply {
                moveTo(pts[0].x, pts[0].y)
                for (i in 1 until pts.size) lineTo(pts[i].x, pts[i].y)
                close()
            }
            drawPath(p, c, style = if (fill) Fill else line)
        }
        when (shape) {
            GlyphShape.Diamond, GlyphShape.DiamondFilled -> poly(
                pts = listOf(
                    Offset(s / 2, 0f),
                    Offset(s, s / 2),
                    Offset(s / 2, s),
                    Offset(0f, s / 2),
                ),
                fill = shape == GlyphShape.DiamondFilled,
            )

            GlyphShape.Triangle -> poly(pts = listOf(Offset(s / 2, 0f), Offset(s, s), Offset(0f, s)), fill = false)

            GlyphShape.TriangleDown -> poly(pts = listOf(Offset(0f, 0f), Offset(s, 0f), Offset(s / 2, s)), fill = false)

            GlyphShape.Play -> poly(pts = listOf(Offset(0f, 0f), Offset(s, s / 2), Offset(0f, s)), fill = true)

            GlyphShape.Cross -> {
                drawLine(c, Offset(s / 2, 0f), Offset(s / 2, s), stroke)
                drawLine(c, Offset(0f, s / 2), Offset(s, s / 2), stroke)
            }

            GlyphShape.Register -> {
                drawCircle(c, s * 0.32f, Offset(s / 2, s / 2), style = line)
                drawLine(c, Offset(s / 2, 0f), Offset(s / 2, s), stroke)
                drawLine(c, Offset(0f, s / 2), Offset(s, s / 2), stroke)
            }
        }
    }
}

internal enum class MarkShape { Check, Underline, Circle, Arrow, Cross }

internal class MarkDecal(
    shape: MarkShape,
    private val w: Float,
    private val h: Float,
    private val stroke: Float,
    j: FloatArray,
) : Decal {
    override val extent = Size(w, h)
    private val path = Path().apply {
        when (shape) {
            MarkShape.Check -> {
                moveTo(w * (0.05f + j[0]), h * (0.55f + j[1]))
                lineTo(w * (0.35f + j[2]), h * (0.95f + j[3]))
                lineTo(w * (0.95f + j[4]), h * (0.05f + j[5]))
            }

            MarkShape.Underline -> {
                moveTo(0f, h * (0.6f + j[0]))
                quadraticTo(w * 0.5f, h * (0.35f + j[1]), w, h * (0.7f + j[2]))
                moveTo(w * 0.1f, h * (0.9f + j[3]))
                quadraticTo(w * 0.5f, h * (0.75f + j[4]), w * 0.9f, h * (0.95f + j[5]))
            }

            MarkShape.Circle -> arcTo(
                Rect(w * j[0], h * j[1], w * (1f + j[2]), h * (1f + j[3])),
                startAngleDegrees = -200f + j[4] * 60f,
                sweepAngleDegrees = 390f,
                forceMoveTo = true,
            )

            MarkShape.Arrow -> {
                moveTo(0f, h * (0.5f + j[0]))
                quadraticTo(w * 0.5f, h * (0.5f + j[1]), w, h * 0.5f)
                moveTo(w * 0.78f, h * (0.12f + j[2]))
                lineTo(w, h * 0.5f)
                lineTo(w * 0.78f, h * (0.88f + j[3]))
            }

            MarkShape.Cross -> {
                moveTo(w * (0.05f + j[0]), h * 0.05f)
                lineTo(w * 0.95f, h * (0.95f + j[1]))
                moveTo(w * 0.95f, h * (0.05f + j[2]))
                lineTo(w * (0.05f + j[3]), h * 0.95f)
            }
        }
    }
    private val style = Stroke(stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)

    override fun DrawScope.draw(color: Color, alpha: Float) {
        drawPath(path, color.copy(alpha = alpha), style = style)
    }
}

internal class TapeDecal(private val w: Float, private val h: Float) : Decal {
    override val extent = Size(w, h)
    private val n = h * 0.25f
    private val path = Path().apply {
        moveTo(0f, 0f)
        lineTo(w, 0f)
        lineTo(w - n, h / 2f)
        lineTo(w, h)
        lineTo(0f, h)
        lineTo(n, h / 2f)
        close()
    }

    override fun DrawScope.draw(color: Color, alpha: Float) {
        drawPath(path, color.copy(alpha = alpha * 0.5f))
    }
}

internal class RingDecal(private val r: Float, private val stroke: Float) : Decal {
    override val extent = Size(r * 2f, r * 2f)
    override fun DrawScope.draw(color: Color, alpha: Float) {
        val c = color.copy(alpha = alpha)
        drawCircle(c, r * 0.95f, Offset(r, r), style = Stroke(stroke))
        drawCircle(c.copy(alpha = alpha * 0.5f), r * 0.8f, Offset(r * 1.05f, r), style = Stroke(stroke * 0.6f))
    }
}

internal class StitchDecal(private val len: Float, private val dash: Float, private val stroke: Float) : Decal {
    override val extent = Size(len, stroke * 2f)
    override fun DrawScope.draw(color: Color, alpha: Float) {
        val c = color.copy(alpha = alpha)
        var x = 0f
        while (x < len) {
            drawLine(c, Offset(x, stroke), Offset(minOf(x + dash, len), stroke), stroke)
            x += dash * 1.8f
        }
    }
}

internal class StapleDecal(private val w: Float, private val h: Float, private val stroke: Float) : Decal {
    override val extent = Size(w, h)
    private val path = Path().apply {
        moveTo(0f, h)
        lineTo(0f, h * 0.2f)
        lineTo(w, h * 0.2f)
        lineTo(w, h)
    }

    override fun DrawScope.draw(color: Color, alpha: Float) {
        drawPath(path, color.copy(alpha = alpha), style = Stroke(stroke * 1.4f, join = StrokeJoin.Round))
    }
}

internal class CornerFoldDecal(private val s: Float) : Decal {
    override val extent = Size(s, s)
    private val path = Path().apply {
        moveTo(0f, 0f)
        lineTo(s, 0f)
        lineTo(0f, s)
        close()
    }
    override fun DrawScope.draw(color: Color, alpha: Float) {
        drawPath(path, color.copy(alpha = alpha * 0.6f))
        drawLine(color.copy(alpha = alpha), Offset(s, 0f), Offset(0f, s), 1f)
    }
}

internal class TallyDecal(
    private val count: Int,
    private val h: Float,
    private val gap: Float,
    private val stroke: Float,
) : Decal {
    private val groups = (count + 4) / 5
    override val extent = Size(groups * gap * 6f, h)
    override fun DrawScope.draw(color: Color, alpha: Float) {
        val c = color.copy(alpha = alpha)
        for (g in 0 until groups) {
            val n = minOf(5, count - g * 5)
            val x0 = g * gap * 6f
            for (i in 0 until minOf(n, 4)) {
                drawLine(c, Offset(x0 + i * gap, 0f), Offset(x0 + i * gap, h), stroke, StrokeCap.Round)
            }
            if (n == 5) {
                drawLine(
                    c,
                    Offset(x0 - gap * 0.4f, h * 0.85f),
                    Offset(x0 + gap * 3.4f, h * 0.15f),
                    stroke,
                    StrokeCap.Round,
                )
            }
        }
    }
}
