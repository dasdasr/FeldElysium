package com.example.feldelysium.designsystem.shape

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlin.random.Random

class RoughShape(
    val seed: Int,
    private val roughness: Dp = 2.5.dp,
    private val corner: Dp = 7.dp,
    private val step: Dp = 40.dp,
) : Shape {

    fun rimPx(density: Density): Float = with(density) { roughness.toPx() } * 1.5f

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline = Outline.Generic(buildPath(size, density))

    fun buildPath(size: Size, density: Density): Path {
        if (size.minDimension <= 0f) return Path()

        val amp = with(density) { roughness.toPx() }
        val rim = amp * 1.5f
        val stepPx = with(density) { step.toPx() }
        val cornerPx = with(density) { corner.toPx() }

        val rnd = Random(seed)
        fun j(a: Float) = (rnd.nextFloat() * 2f - 1f) * a

        val corners = listOf(
            Offset(rim, rim),
            Offset(size.width - rim, rim),
            Offset(size.width - rim, size.height - rim),
            Offset(rim, size.height - rim),
        ).map { Offset(it.x + j(amp * 0.6f), it.y + j(amp * 0.6f)) }
        val radius = FloatArray(4) { cornerPx * (0.7f + rnd.nextFloat() * 0.6f) }

        val sides = List(4) { i ->
            val a = corners[i]
            val b = corners[(i + 1) % 4]
            val dir = (b - a) / (b - a).getDistance()
            val normal = Offset(-dir.y, dir.x)
            val s = a + dir * radius[i]
            val e = b - dir * radius[(i + 1) % 4]
            val n = ((e - s).getDistance() / stepPx).roundToInt().coerceAtLeast(1)
            List(n + 1) { k ->
                val base = lerp(s, e, k / n.toFloat())
                val slide = if (k in 1 until n) j(stepPx * 0.12f) else 0f
                base + normal * j(amp) + dir * slide
            }
        }

        return Path().apply {
            moveTo(sides[0][0].x, sides[0][0].y)
            for (i in 0 until 4) {
                catmullRomTo(sides[i])
                val c = corners[(i + 1) % 4]
                val next = sides[(i + 1) % 4][0]
                quadraticTo(c.x, c.y, next.x, next.y)
            }
            close()
        }
    }
}

private fun Path.catmullRomTo(pts: List<Offset>) {
    for (i in 0 until pts.lastIndex) {
        val p0 = pts[maxOf(i - 1, 0)]
        val p1 = pts[i]
        val p2 = pts[i + 1]
        val p3 = pts[minOf(i + 2, pts.lastIndex)]
        cubicTo(
            p1.x + (p2.x - p0.x) / 6f, p1.y + (p2.y - p0.y) / 6f,
            p2.x - (p3.x - p1.x) / 6f, p2.y - (p3.y - p1.y) / 6f,
            p2.x, p2.y,
        )
    }
}
