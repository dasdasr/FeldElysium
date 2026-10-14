package com.example.feldelysium.designsystem.foundation.decal

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize

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

internal fun DrawScope.drawDecals(list: List<Placement>) {
    for (placement in list) {
        val c = placement.bounds.center
        val e = placement.decal.extent
        withTransform({
            rotate(placement.rotation, c)
            if (placement.mirror) scale(-1f, 1f, c)
            translate(c.x - e.width / 2f, c.y - e.height / 2f)
        }) {
            with(placement.decal) { draw(placement.color, placement.alpha) }
        }
    }
}

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

internal class GlyphDecal(
    private val shape: GlyphShape,
    private val side: Float,
    private val stroke: Float,
) : Decal {
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
                quadraticBezierTo(w * 0.5f, h * (0.35f + j[1]), w, h * (0.7f + j[2]))
                moveTo(w * 0.1f, h * (0.9f + j[3]))
                quadraticBezierTo(w * 0.5f, h * (0.75f + j[4]), w * 0.9f, h * (0.95f + j[5]))
            }
            MarkShape.Circle -> arcTo(
                Rect(w * j[0], h * j[1], w * (1f + j[2]), h * (1f + j[3])),
                startAngleDegrees = -200f + j[4] * 60f,
                sweepAngleDegrees = 390f,
                forceMoveTo = true,
            )
            MarkShape.Arrow -> {
                moveTo(0f, h * (0.5f + j[0]))
                quadraticBezierTo(w * 0.5f, h * (0.5f + j[1]), w, h * 0.5f)
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

internal class SpriteDecal(
    private val bitmap: ImageBitmap,
    private val src: IntRect,
    override val extent: Size,
) : Decal {
    override fun DrawScope.draw(color: Color, alpha: Float) {
        drawImage(
            image = bitmap,
            srcOffset = IntOffset(src.left, src.top),
            srcSize = IntSize(src.width, src.height),
            dstSize = IntSize(extent.width.toInt(), extent.height.toInt()),
            alpha = alpha,
            colorFilter = ColorFilter.tint(color),
        )
    }
}
