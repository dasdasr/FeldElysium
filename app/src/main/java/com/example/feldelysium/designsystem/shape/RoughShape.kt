package com.example.feldelysium.designsystem.shape

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
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

    val inkVariation: Float = 0.4f,
    val inkNoiseStep: Dp = 140.dp,
    val inkFineStep: Dp = 16.dp,
    val inkPressStep: Dp = 360.dp,
    val inkPressRamp: Dp = 20.dp,
    val inkMinAlpha: Float = 0.5f,
    val inkContrast: Float = 1.5f,
    val inkWidthCorrelation: Float = 0.5f,
    val inkLevels: Int = 10,
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
}


class InkLayer(val path: Path, val alpha: Float)

private class LoopNoise(seed: Int, private val len: Float, cellPx: Float) {
    private val cells = (len / cellPx).roundToInt().coerceAtLeast(3)
    private val table = Random(seed).let { r -> FloatArray(cells) { r.nextFloat() } }

    fun at(s: Float): Float {
        val x = s / len * cells
        val i = floor(x).toInt()
        val f = x - i
        val u = f * f * (3f - 2f * f)
        val a = table[i.mod(cells)]
        val b = table[(i + 1).mod(cells)]
        return a + (b - a) * u
    }
}

private class LoopSteps(seed: Int, len: Float, cellPx: Float, private val rampPx: Float) {
    private val starts = ArrayList<Float>()
    private val vals = ArrayList<Float>()

    init {
        val rnd = Random(seed)
        var s = 0f
        while (s < len) {
            starts += s
            vals += rnd.nextFloat()
            s += cellPx * (0.7f + rnd.nextFloat() * 0.8f)
        }
    }

    fun at(s: Float): Float {
        val i = starts.indexOfLast { it <= s }.coerceAtLeast(0)
        val prev = vals[(i - 1 + vals.size) % vals.size]
        val u = ((s - starts[i]) / rampPx).coerceIn(0f, 1f)
        return prev + (vals[i] - prev) * (u * u * (3f - 2f * u))
    }
}

fun RoughShape.buildInkLayers(
    outline: Path,
    density: Density,
    baseWidth: Float,
): List<InkLayer> {
    val measure = PathMeasure().apply { setPath(outline, true) }
    val len = measure.length
    if (len <= 0f) return emptyList()

    val cell = with(density) { style.inkNoiseStep.toPx() }
    val fineCell = with(density) { style.inkFineStep.toPx() }
    val pressCell = with(density) { style.inkPressStep.toPx() }
    val ramp = with(density) { style.inkPressRamp.toPx() }

    val slow = LoopNoise(seed * 31 + 7, len, cell)
    val fine = LoopNoise(seed * 13 + 5, len, fineCell)
    val press = LoopSteps(seed * 7 + 11, len, pressCell, ramp)
    val widthNoise = LoopNoise(seed * 17 + 3, len, cell * 1.4f)

    val levels = style.inkLevels.coerceAtLeast(2)
    val samples = (len / with(density) { 2.5.dp.toPx() }).toInt().coerceAtLeast(16)

    val outer = ArrayList<Offset>(samples)
    val inner = ArrayList<Offset>(samples)
    val levelOf = IntArray(samples)

    for (i in 0 until samples) {
        val s = len * i / samples
        val pos = measure.getPosition(s)
        val tan = measure.getTangent(s)
        val nrm = Offset(-tan.y, tan.x)

        val raw = 0.55f * slow.at(s) + 0.3f * press.at(s) + 0.15f * fine.at(s)
        val n = ((raw - 0.5f) * style.inkContrast + 0.5f).coerceIn(0f, 1f)
        levelOf[i] = (n * (levels - 1)).roundToInt()

        val c = style.inkWidthCorrelation.coerceIn(0f, 1f)
        val wn = c * n + (1f - c) * widthNoise.at(s)
        val w = (baseWidth * (1f + style.inkVariation * (wn * 2f - 1f)))
            .coerceIn(baseWidth * 0.5f, baseWidth * 1.8f)

        outer += pos + nrm * (w * 0.5f)
        inner += pos - nrm * (w * 0.5f)
    }

    val paths = Array(levels) { Path() }
    val used = BooleanArray(levels)
    for (i in 0 until samples) {
        val j = (i + 1) % samples
        val p = paths[levelOf[i]]
        used[levelOf[i]] = true
        p.moveTo(outer[i].x, outer[i].y)
        p.lineTo(outer[j].x, outer[j].y)
        p.lineTo(inner[j].x, inner[j].y)
        p.lineTo(inner[i].x, inner[i].y)
        p.close()
    }

    return paths.indices.filter { used[it] }.map {
        val t = it / (levels - 1).toFloat()
        InkLayer(paths[it], style.inkMinAlpha + (1f - style.inkMinAlpha) * t)
    }
}

fun DrawScope.drawInk(layers: List<InkLayer>, brush: Brush) {
    for (layer in layers) drawPath(layer.path, brush, alpha = layer.alpha)
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
