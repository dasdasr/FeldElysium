package com.example.feldelysium.designsystem.shape

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
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

    val jerkChance: Float = 0.14f,
    val jerkAmp: Float = 1.3f,
    val jerkWidth: Dp = 3.dp,

    val tornChance: Float = 0.18f,
    val forceTorn: Int = -1,
    val tornPush: Float = 0.6f,

    val inkVariation: Float = 0.55f,
    val inkJump: Float = 0.45f,
    val inkNoiseStep: Dp = 36.dp,
)

private data class Knot(val p: Offset, val jumpNext: Boolean = false)

class RoughShape(
    val seed: Int,
    val style: RoughStyle = RoughStyle(),
) : Shape {

    fun rimPx(density: Density): Float = with(density) {
        style.roughness.toPx() * (1.5f + style.jerkAmp + style.cornerJitter) + style.inset.toPx()
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
        val jw = with(density) { style.jerkWidth.toPx() }
        val smooth = style.smoothness.coerceIn(0f, 0.95f)

        val rnd = Random(seed)
        fun j(a: Float) = (rnd.nextFloat() * 2f - 1f) * a

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

        val torn = BooleanArray(4) { it == style.forceTorn || rnd.nextFloat() < style.tornChance }

        val cj = amp * style.cornerJitter
        val sx = floatArrayOf(-1f, 1f, 1f, -1f)
        val sy = floatArrayOf(-1f, -1f, 1f, 1f)
        val base4 = listOf(
            Offset(rim, rim),
            Offset(size.width - rim, rim),
            Offset(size.width - rim, size.height - rim),
            Offset(rim, size.height - rim),
        )
        val corners = base4.mapIndexed { i, o ->
            val push = if (torn[i]) rim * style.tornPush else 0f
            Offset(o.x + j(cj) + sx[i] * push, o.y + j(cj) + sy[i] * push)
        }
        val radius = FloatArray(4) { i ->
            val r =
                cornerPx * (1f - style.cornerVariation + rnd.nextFloat() * 2f * style.cornerVariation)
            if (torn[i]) r * 0.15f else r
        }

        var perimeter = 0f
        var drift = 0f
        var jerkOff = 0f
        val maxDrift = amp * 1.5f

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

            buildList {
                for (k in 0..n) {
                    val t = k / n.toFloat()
                    val base = lerp(start, end, t)
                    drift = drift * smooth + j(1f) * (1f - smooth)
                    val dev = (drift * amp * envelope(sideStart + t * length) * 1.6f)
                        .coerceIn(-maxDrift, maxDrift)
                    val slide = if (k in 1 until n) j(stepPx * style.slide) else 0f

                    val jerk = k in 1 until n && rnd.nextFloat() < style.jerkChance
                    if (jerk) {
                        add(
                            Knot(
                                base + normal * (dev + jerkOff) + dir * (slide - jw * 0.5f),
                                jumpNext = true
                            )
                        )
                        val sign = if (rnd.nextBoolean()) 1f else -1f
                        jerkOff = sign * (0.5f + rnd.nextFloat() * 0.5f) * amp * style.jerkAmp
                        add(Knot(base + normal * (dev + jerkOff) + dir * (slide + jw * 0.5f)))
                    } else {
                        jerkOff *= 0.6f
                        add(Knot(base + normal * (dev + jerkOff) + dir * slide))
                    }
                }
            }
        }

        return Path().apply {
            moveTo(sides[0][0].p.x, sides[0][0].p.y)
            for (i in 0 until 4) {
                catmullRomTo(sides[i])
                val ci = (i + 1) % 4
                val c = corners[ci]
                val next = sides[ci][0].p
                if (torn[ci]) {
                    lineTo(c.x, c.y)
                    lineTo(next.x, next.y)
                } else {
                    quadraticTo(c.x, c.y, next.x, next.y)
                }
            }
            close()
        }
    }

    fun buildInkPath(outline: Path, density: Density, baseWidth: Float): Path {
        val measure = PathMeasure().apply { setPath(outline, true) }
        val len = measure.length
        if (len <= 0f) return Path()

        val rnd = Random(seed * 31 + 7)
        val cell = with(density) { style.inkNoiseStep.toPx() }
        val ramp = with(density) { 3.dp.toPx() }

        val cells = (len / cell).roundToInt().coerceAtLeast(4)
        val smoothT = FloatArray(cells) { rnd.nextFloat() }
        fun smoothAt(s: Float): Float {
            val x = s / len * cells
            val i = floor(x).toInt()
            val f = x - i
            val u = f * f * (3f - 2f * f)
            val a = smoothT[i.mod(cells)]
            val b = smoothT[(i + 1).mod(cells)]
            return a + (b - a) * u
        }

        val starts = ArrayList<Float>()
        val vals = ArrayList<Float>()
        var s0 = 0f
        while (s0 < len) {
            starts += s0
            vals += rnd.nextFloat()
            s0 += cell * (0.8f + rnd.nextFloat() * 2.2f)
        }
        fun stepAt(s: Float): Float {
            val i = starts.indexOfLast { it <= s }.coerceAtLeast(0)
            val prev = vals[(i - 1 + vals.size) % vals.size]
            val u = ((s - starts[i]) / ramp).coerceIn(0f, 1f)
            return prev + (vals[i] - prev) * (u * u * (3f - 2f * u))
        }

        fun widthAt(s: Float): Float {
            val w = baseWidth *
                    (1f + style.inkVariation * (smoothAt(s) * 2f - 1f)) *
                    (1f + style.inkJump * (stepAt(s) * 2f - 1f))
            return w.coerceIn(baseWidth * 0.35f, baseWidth * 2.4f)
        }

        val samples = (len / with(density) { 2.5.dp.toPx() }).toInt().coerceAtLeast(16)
        val outer = ArrayList<Offset>(samples)
        val inner = ArrayList<Offset>(samples)
        for (i in 0 until samples) {
            val s = len * i / samples
            val pos = measure.getPosition(s)
            val tan = measure.getTangent(s)
            val nrm = Offset(-tan.y, tan.x)
            val h = widthAt(s) * 0.5f
            outer += pos + nrm * h
            inner += pos - nrm * h
        }

        return Path().apply {
            moveTo(outer[0].x, outer[0].y)
            for (i in 1 until outer.size) lineTo(outer[i].x, outer[i].y)
            close()
            moveTo(inner.last().x, inner.last().y)
            for (i in inner.size - 2 downTo 0) lineTo(inner[i].x, inner[i].y)
            close()
        }
    }
}

private fun Path.catmullRomTo(k: List<Knot>) {
    for (i in 0 until k.lastIndex) {
        val p1 = k[i].p
        val p2 = k[i + 1].p
        if (k[i].jumpNext) {
            lineTo(p2.x, p2.y); continue
        }

        val prevJump = i > 0 && k[i - 1].jumpNext
        val nextJump = k[i + 1].jumpNext
        val p0 = if (i == 0 || prevJump) p1 else k[i - 1].p
        val p3 = if (i + 2 > k.lastIndex || nextJump) p2 else k[i + 2].p
        cubicTo(
            p1.x + (p2.x - p0.x) / 6f, p1.y + (p2.y - p0.y) / 6f,
            p2.x - (p3.x - p1.x) / 6f, p2.y - (p3.y - p1.y) / 6f,
            p2.x, p2.y,
        )
    }
}
