package com.example.feldelysium.eggs.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.feldelysium.designsystem.foundation.material.accentBrush
import com.example.feldelysium.designsystem.foundation.material.wornBrush
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

private const val BurstMs = 560

class ArcSeg(val path: Path, val alpha: Float, val frac: Float)
private class Rib(val a: Offset, val b: Offset, val u: Float)
private class Streak(val angle: Float, val len: Float, val delay: Float, val jit: List<Float>, val nick: Float)

@Composable
internal fun ToastDecor(accent: Color, seed: Int, modifier: Modifier = Modifier) {
    val reveal = remember { Animatable(0f) }
    val burst = remember { Animatable(0f) }
    val pulse = remember { Animatable(1f) }
    val ripple = remember { Animatable(1f) }
    var volley by remember { mutableIntStateOf(0) }
    val bob by rememberInfiniteTransition(label = "bob").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(900, easing = FastOutSlowInEasing),
            RepeatMode.Reverse,
        ),
        label = "bobValue",
    )

    LaunchedEffect(Unit) { reveal.animateTo(1f, tween(520, easing = LinearOutSlowInEasing)) }

    LaunchedEffect(Unit) {
        delay(150)
        burst.animateTo(1f, tween(BurstMs, easing = LinearEasing))
    }

    LaunchedEffect(Unit) {
        delay(900)
        while (true) {
            launch {
                ripple.snapTo(0f);
                ripple.animateTo(1f, tween(700))
            }
            launch {
                volley++
                burst.snapTo(0f)
                burst.animateTo(1f, tween(BurstMs, easing = LinearEasing))
            }
            pulse.animateTo(1.22f, tween(70));
            pulse.animateTo(1f, tween(90))
            pulse.animateTo(1.14f, tween(70));
            pulse.animateTo(1f, tween(140))
            delay(Random.nextLong(1300, 2300))
        }
    }

    Box(
        modifier.drawWithCache {
            val d = density
            val rnd = Random(seed + 11)
            val cardL = PadStart.toPx()
            val cardT = PadTop.toPx()
            val cardB = size.height - PadBottom.toPx()
            val cardR = size.width
            val cardH = cardB - cardT

            val R = 12 * d
            val arcR = cardH * 0.67f
            val overhang = PadStart.toPx() - 3 * d
            val cx = cardL - overhang + arcR
            val topY = cardT - R + 3 * d
            val cy = topY + arcR
            val sinEnd = ((cardB + 3 * d - cy) / arcR).coerceIn(-1f, 1f)
            val startDeg = -90f
            val endDeg = Math.toDegrees(asin(sinEnd.toDouble())).toFloat() - 360f

            val segs = inkArc(Offset(cx, cy), arcR, arcR, startDeg, endDeg, d, rnd)
            val c = Offset(cx, topY)

            val volleys = List(6) { idx ->
                val base = if (idx == 0) -58f else -66f + rnd.nextFloat() * 22f
                val second = if (idx == 0) -24f else base + 26f + rnd.nextFloat() * 10f
                val angles = when {
                    idx == 0 || rnd.nextBoolean() -> listOf(base, second)
                    else -> listOf(if (rnd.nextBoolean()) base else second)
                }
                angles.mapIndexed { k, a ->
                    Streak(
                        angle = a,
                        len = (13f + rnd.nextFloat() * 7f) * d,
                        delay = if (k == 0) 0f else 0.14f,
                        jit = List(12) { rnd.nextFloat() * 2f - 1f },
                        nick = 0.35f + rnd.nextFloat() * 0.3f,
                    )
                }
            }

            val fill = accentBrush(accent, seed + 2, d)
            val arcBrush = wornBrush(Cream, seed + 7, d, wear = 0.5f)

            onDrawBehind {
                val rv = reveal.value
                segs.forEach { if (it.frac <= rv) drawPath(it.path, arcBrush) }

                val appear = min(1f, rv * 6f)
                if (ripple.value < 1f) {
                    drawCircle(
                        accent.copy(alpha = (1f - ripple.value) * 0.6f),
                        R * appear + 12 * d * ripple.value,
                        c,
                        style = Stroke(1.5f * d),
                    )
                }
                val s = pulse.value
                drawCircle(fill, R * s * appear, c)
                drawCircle(InkDeep, (R - 3.8f * d) * s * appear, c)
                drawCircle(Color(0xFFF3ECDD), 4.6f * d * (1f + (s - 1f) * 1.8f) * appear, c)

                val b = burst.value
                if (b > 0f && b < 1f) {
                    volleys[volley % volleys.size].forEach { st ->
                        val u = ((b - st.delay) / (1f - st.delay)).coerceIn(0f, 1f)
                        if (u <= 0f || u >= 1f) return@forEach
                        val inv = 1f - u
                        val r0 = R + 3f * d
                        val head = r0 + st.len * (1f - inv * inv * inv)
                        val tail = r0 + st.len * (u * u * u)
                        val len = head - tail
                        if (len < 1f * d) return@forEach
                        val rad = Math.toRadians(st.angle.toDouble())
                        val dir = Offset(cos(rad).toFloat(), sin(rad).toFloat())
                        drawFlick(
                            from = c + dir * head,
                            dir = -dir,
                            len = len,
                            w = 3.2f * d,
                            color = Cream.copy(alpha = 1f - u * u),
                            jit = st.jit,
                            nick = st.nick,
                            d = d,
                        )
                    }
                }

                val tx = cardL + (cardR - cardL) / 2f
                val ty = cardB + (8f + bob * 3f) * d
                drawPath(
                    Path().apply {
                        moveTo(tx - 6 * d, ty);
                        lineTo(tx + 6 * d, ty);
                        lineTo(tx, ty + 7 * d);
                        close()
                    },
                    Cream.copy(alpha = 0.9f),
                )
            }
        },
    )
}

private fun inkArc(
    c: Offset,
    rx: Float,
    ry: Float,
    startDeg: Float,
    endDeg: Float,
    d: Float,
    rnd: Random,
): List<ArcSeg> {
    val steps = ((startDeg - endDeg) / 2f).roundToInt()
    val ph = List(2) { rnd.nextFloat() * 6.28f }
    fun noise(u: Float, f1: Float, f2: Float) = 0.5f + 0.25f * sin(u * f1 + ph[0]) + 0.25f * sin(u * f2 + ph[1])

    val ribs = List(steps + 1) { i ->
        val u = i / steps.toFloat()
        val r = Math.toRadians((startDeg + (endDeg - startDeg) * u).toDouble())
        val cs = cos(r).toFloat()
        val sn = sin(r).toFloat()
        val n = Offset(ry * cs, rx * sn).let { it / it.getDistance() }
        val taper = min(1f, min(u * 14f, (1f - u) * 9f)).coerceAtLeast(0.15f)
        val w = (1.2f + 2f * noise(u, 9f, 23f)) * d * taper
        val p = Offset(c.x + rx * cs, c.y + ry * sn) + n * ((rnd.nextFloat() - 0.5f) * 0.5f * d)
        Rib(p - n * (w / 2f), p + n * (w / 2f), u)
    }

    val segs = ArrayList<ArcSeg>()
    var i = 0
    var gap = false
    while (i < steps) {
        val j = min(i + 3, steps)
        val u = ribs[i].u
        gap = u > 0.5f && !gap && rnd.nextFloat() < 0.22f
        if (gap) {
            i = min(i + 3 + rnd.nextInt(3), steps)
            continue
        }
        val path = Path().apply {
            moveTo(ribs[i].a.x, ribs[i].a.y)
            for (k in i + 1..j) lineTo(ribs[k].a.x, ribs[k].a.y)
            for (k in j downTo i) lineTo(ribs[k].b.x, ribs[k].b.y)
            close()
        }
        segs.add(ArcSeg(path, (0.35f + 0.65f * noise(u, 14f, 31f)).coerceIn(0.3f, 1f), u))
        i = j
    }
    return segs
}

private fun DrawScope.drawFlick(
    from: Offset,
    dir: Offset,
    len: Float,
    w: Float,
    color: Color,
    jit: List<Float>,
    nick: Float,
    d: Float,
) {
    val nrm = Offset(-dir.y, dir.x)
    val n = jit.size / 2
    fun half(i: Int, side: Int): Float {
        val t = i / (n - 1f)
        return (w * (1f - 0.8f * t) / 2f + jit[side * n + i] * 0.5f * d).coerceAtLeast(0.2f * d)
    }

    val path = Path()
    for (i in 0 until n) {
        val p = from + dir * (len * i / (n - 1f)) + nrm * half(i, 0)
        if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
    }
    for (i in n - 1 downTo 0) {
        val p = from + dir * (len * i / (n - 1f)) - nrm * half(i, 1)
        path.lineTo(p.x, p.y)
    }
    path.close()
    drawPath(path, color)
    val m = from + dir * (len * nick)
    drawLine(InkDeep, m - nrm * w, m + nrm * w, 1.1f * d)
}
