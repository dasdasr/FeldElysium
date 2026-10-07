package com.example.feldelysium.designsystem.shape

import androidx.compose.runtime.Immutable
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
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random

@Immutable
data class RoughStyle(
    val roughness: Dp = 1.2.dp,
    val irregularity: Float = 0.65f,
    val smoothness: Float = 0.55f,
    val step: Dp = 56.dp,
    val slide: Float = 0.12f,
    val corner: Dp = 6.dp,
    val cornerVariation: Float = 0.35f,
    val cornerJitter: Float = 0.5f,
    val inset: Dp = 1.5.dp,
)

class RoughShape(
    val seed: Int,
    val style: RoughStyle = RoughStyle(),
) : Shape {

    fun rimPx(density: Density): Float = with(density) {
        style.roughness.toPx() * (1.5f + style.cornerJitter) + style.inset.toPx()
    }

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline = Outline.Generic(buildPath(size, density))

    fun buildPath(size: Size, density: Density): Path {
        if (size.minDimension <= 0f) return Path()

        val amp = with(density) { style.roughness.toPx() }
        val rim = rimPx(density)
        val stepPx = with(density) { style.step.toPx() }
        val cornerPx = with(density) { style.corner.toPx() }
        val smooth = style.smoothness.coerceIn(0f, 0.95f)

        val rnd = Random(seed)
        fun j(a: Float) = (rnd.nextFloat() * 2f - 1f) * a

        // 1D value noise along the perimeter: decides where the edge is calm and where it is rough
        val table = FloatArray(32) { rnd.nextFloat() }
        fun envelope(s: Float): Float {
            val x = s / (stepPx * 2.5f)
            val i = floor(x).toInt()
            val f = x - i
            val u = f * f * (3f - 2f * f)
            val a = table[i.mod(32)]
            val b = table[(i + 1).mod(32)]
            val v = (a + (b - a) * u).pow(1.5f)
            return (1f - style.irregularity) + v * (style.irregularity * 1.5f)
        }

        val cj = amp * style.cornerJitter
        val corners = listOf(
            Offset(rim, rim),
            Offset(size.width - rim, rim),
            Offset(size.width - rim, size.height - rim),
            Offset(rim, size.height - rim),
        ).map { Offset(it.x + j(cj), it.y + j(cj)) }
        val radius = FloatArray(4) {
            cornerPx * (1f - style.cornerVariation + rnd.nextFloat() * 2f * style.cornerVariation)
        }

        var perimeter = 0f
        var drift = 0f
        val sides = List(4) { i ->
            val a = corners[i]
            val b = corners[(i + 1) % 4]
            val length = (b - a).getDistance()
            val dir = (b - a) / length
            val normal = Offset(-dir.y, dir.x)
            val start = a + dir * radius[i]
            val end = b - dir * radius[(i + 1) % 4]
            val n = ((end - start).getDistance() / stepPx).roundToInt().coerceAtLeast(1)
            val sideStart = perimeter
            perimeter += length

            List(n + 1) { k ->
                val t = k / n.toFloat()
                val base = lerp(start, end, t)
                drift = drift * smooth + j(1f) * (1f - smooth)
                val dev = (drift * amp * envelope(sideStart + t * length) * 1.6f)
                    .coerceIn(-amp * 1.5f, amp * 1.5f)
                val slide = if (k in 1 until n) j(stepPx * style.slide) else 0f
                base + normal * dev + dir * slide
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
