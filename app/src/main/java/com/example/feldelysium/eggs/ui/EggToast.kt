package com.example.feldelysium.eggs.ui

import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.feldelysium.designsystem.components.speaker.FeldSpeakerDiamond
import com.example.feldelysium.designsystem.components.title.FeldTitleTag
import com.example.feldelysium.domain.model.AttributeGroup
import com.example.feldelysium.domain.model.Speaker
import com.example.feldelysium.domain.registry.PredefinedSpeakers
import com.example.feldelysium.eggs.CheckResult
import com.example.feldelysium.eggs.Difficulty
import com.example.feldelysium.eggs.EasterEggs
import com.example.feldelysium.eggs.EggReply
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

private val Ink = Color(0xFF131616)
private val InkDeep = Color(0xFF0B0E0E)
private val Cream = Color(0xFFE9DFC9)
private val CreamDim = Color(0xFF8F8B7E)
private val TextCream = Color(0xFFEFE8D6)

private val PadStart = 26.dp
private val PadTop = 40.dp
private val PadBottom = 24.dp
private val ToastHeadroom = 120.dp
private val ToastFadeDistance = 100.dp
private const val ToastBgAlpha = 0.90f

private fun skillColor(skill: Speaker): Color = when (skill.attributeGroup) {
    AttributeGroup.INTELLECT -> Color(0xFF4FB3A9)
    AttributeGroup.PSYCHE -> Color(0xFF8E7FCB)
    AttributeGroup.PHYSIQUE -> Color(0xFFC9564B)
    AttributeGroup.MOTORICS -> Color(0xFFD8B54A)
    else -> Color(skill.colorArgb)
}

private class ToastView(val skill: Speaker, val text: String, val check: String?)

private fun EggReply.toView() = ToastView(
    skill = skill,
    text = text,
    check = difficulty?.let { "[$it: ${result?.label ?: "Success"}]" },
)

@Composable
fun EggToastHost(eggs: EasterEggs, modifier: Modifier = Modifier) {
    val toast by eggs.toast.collectAsStateWithLifecycle()
    var last by remember { mutableStateOf<EasterEggs.ActiveToast?>(null) }
    LaunchedEffect(toast) { toast?.let { last = it } }
    val current = toast ?: last

    val visible = remember { MutableTransitionState(false) }
    visible.targetState = toast != null

    if (visible.currentState || visible.targetState) {
        ToastWindow {
            Box(
                modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 6.dp)
                    .padding(top = ToastHeadroom),
                contentAlignment = Alignment.BottomCenter,
            ) {
                AnimatedVisibility(
                    visibleState = visible,
                    enter = fadeIn(tween(160)) + slideInVertically(tween(260)) { it / 2 },
                    exit = fadeOut(tween(200)) + slideOutVertically(tween(220)) { it / 2 },
                ) {
                    current?.let { toast ->
                        key(toast.id) { EggToast(toast, eggs::dismiss) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToastWindow(content: @Composable () -> Unit) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        val window = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect {
            window?.apply {
                clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                addFlags(
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                )
                setDimAmount(0f)
                setGravity(Gravity.BOTTOM)
                setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
        }
        content()
    }
}

@Composable
private fun EggToast(toast: EasterEggs.ActiveToast, onDismiss: (Long) -> Unit) {
    val view = remember(toast.id) { toast.reply.toView() }
    val accent = skillColor(view.skill)
    val seed = remember(toast.id) { toast.id.toInt() * 31 + view.text.hashCode() }

    LaunchedEffect(toast.id) {
        delay(toast.durationMillis)
        onDismiss(toast.id)
    }

    val drag = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val scope = rememberCoroutineScope()

    Box(
        Modifier
            .widthIn(max = 520.dp)
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .graphicsLayer {
                translationX = drag.value.x
                translationY = drag.value.y
                rotationZ = drag.value.x / 60f
                alpha = 1f - (drag.value.getDistance() / ToastFadeDistance.toPx()).coerceIn(0f, 1f)
            }
            .pointerInput(toast.id) {
                val threshold = 64.dp.toPx()
                detectDragGestures(
                    onDrag = { change, delta ->
                        change.consume()
                        scope.launch { drag.snapTo(drag.value + delta) }
                    },
                    onDragEnd = {
                        scope.launch {
                            if (drag.value.getDistance() > threshold) {
                                drag.animateTo(drag.value * 3f, tween(180))
                                onDismiss(toast.id)
                            } else {
                                drag.animateTo(Offset.Zero, spring())
                            }
                        }
                    },
                    onDragCancel = { scope.launch { drag.animateTo(Offset.Zero, spring()) } },
                )
            },
    ) {
        ToastDecor(accent, seed, Modifier.matchParentSize())

        Box(
            Modifier
                .padding(start = PadStart, top = PadTop, bottom = PadBottom)
                .fillMaxWidth()
                .toastCard(seed, accent),
        ) {
            Row(
                Modifier.padding(start = 22.dp, end = 14.dp, top = 10.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FeldSpeakerDiamond(
                    color = accent,
                    size = 26.dp,
                    seed = seed,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .scale(scaleX = 1f, scaleY = 1.3f),
                )
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    FeldTitleTag(
                        text = filmSkillLabel(view.skill.name, seed),
                        seed = seed,
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 20.sp,
                            letterSpacing = 0.4.sp
                        )
                    )
                    Text(
                        text = buildAnnotatedString {
//                            view.check?.let {
//                                withStyle(
//                                    SpanStyle(
//                                        color = accent,
//                                        fontWeight = FontWeight.Bold
//                                    )
//                                ) { append("$it ") }
//                            }
                            append(view.text)
                        },
                        color = TextCream,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

private fun Modifier.toastCard(seed: Int, accent: Color): Modifier = drawWithCache {
    val d = density
    val w = size.width
    val h = size.height
    val rnd = Random(seed)

    val body = roughRectPoints(Rect(0f, 0f, w, h), 15 * d, 1.5f * d, 0.14f, rnd).toPath()
    val band = roughRectPoints(
        Rect(-3 * d, -2 * d, 15 * d, h + 2 * d),
        10 * d,
        1.1f * d,
        0.08f,
        rnd
    ).toPath()
    val rightBand = roughRectPoints(
        Rect(w - 5 * d, 9 * d, w + 0.5f * d, h - 15 * d),
        9 * d,
        0.8f * d,
        0.10f,
        rnd
    ).toPath()

    val frays = ArrayList<Pair<Offset, Offset>>()
    var x = 8 * d
    while (x < w - 12 * d) {
        val len = (5 + rnd.nextFloat() * 30) * d
        if (rnd.nextFloat() < 0.75f)
            frays.add(
                Offset(x, 0.8f * d) to Offset(
                    min(x + len, w - 8 * d),
                    0.8f * d + rnd.nextFloat() * 0.8f * d
                )
            )
        x += len + (4 + rnd.nextFloat() * 26) * d
    }
    var y = 8 * d
    while (y < h - 14 * d) {
        val len = (5 + rnd.nextFloat() * 24) * d
        if (rnd.nextFloat() < 0.7f)
            frays.add(
                Offset(w - 0.8f * d, y) to Offset(
                    w - 0.8f * d - rnd.nextFloat() * 0.8f * d,
                    min(y + len, h - 10 * d)
                )
            )
        y += len + (4 + rnd.nextFloat() * 22) * d
    }

    class Speck(val at: Offset, val s: Float, val light: Boolean, val a: Float)

    val specks = List(120) {
        Speck(
            Offset(rnd.nextFloat() * w, rnd.nextFloat() * h),
            (0.6f + rnd.nextFloat() * 1.6f) * d,
            rnd.nextFloat() < 0.55f,
            0.05f + rnd.nextFloat() * 0.12f,
        )
    }
    val scratches = List(6) {
        val p = Offset(rnd.nextFloat() * w * 0.8f + 20 * d, rnd.nextFloat() * h)
        p to (p + Offset((10 + rnd.nextFloat() * 36) * d, (rnd.nextFloat() - 0.5f) * 8 * d))
    }
    val bandSpecks = List(28) { Offset(rnd.nextFloat() * 15 * d, rnd.nextFloat() * h) }
    val stripe = lerp(accent, Color.Black, 0.3f)

    onDrawBehind {
        drawPath(body, Ink.copy(alpha = ToastBgAlpha))
        specks.forEach {
            drawRect(
                if (it.light) CreamDim.copy(alpha = it.a) else Color.Black.copy(alpha = it.a * 3f),
                it.at, Size(it.s, it.s * 0.6f),
            )
        }
        scratches.forEach { (a, b) -> drawLine(Cream.copy(alpha = 0.06f), a, b, 1f * d) }
        frays.forEach { (a, b) -> drawLine(Color(0xFFA39E8C).copy(alpha = 0.55f), a, b, 1.2f * d) }
        drawPath(body, Color(0xFF2A2D2B), style = Stroke(1f * d))

        drawPath(band, accent)
        clipPath(band) {
            drawRect(stripe, Offset(8 * d, 0f), Size(2 * d, h))
            bandSpecks.forEach {
                drawRect(
                    Color.Black.copy(alpha = 0.3f),
                    it,
                    Size(1.4f * d, 1.4f * d)
                )
            }
        }
        drawPath(rightBand, accent)
    }
}

@Composable
private fun ToastTag(text: String, seed: Int, modifier: Modifier = Modifier) {
    Row(
        modifier
            .drawWithCache {
                val d = density
                val rnd = Random(seed + 3)
                val w = size.width
                val h = size.height
                val pts = buildList<Offset> {
                    add(Offset(0f, rnd.nextFloat() * 1.5f * d))
                    var x = 12 * d
                    while (x < w - 10 * d) {
                        add(Offset(x, (rnd.nextFloat() * 1.6f - 0.4f) * d)); x += 12 * d
                    }
                    add(Offset(w, 0f))
                    add(Offset(w - 3 * d, h * 0.45f))
                    add(Offset(w - 9 * d, h))
                    x = w - 20 * d
                    while (x > 8 * d) {
                        add(Offset(x, h - (rnd.nextFloat() * 1.6f - 0.4f) * d)); x -= 12 * d
                    }
                    add(Offset(0f, h - rnd.nextFloat() * 1.5f * d))
                }
                val path = pts.toPath()
                val cracks = List(3) {
                    val a = Offset(rnd.nextFloat() * w, rnd.nextFloat() * h * 0.5f)
                    a to (a + Offset((rnd.nextFloat() - 0.3f) * 18 * d, h * 0.6f))
                }
                val specks = List(14) { Offset(rnd.nextFloat() * w, rnd.nextFloat() * h) }

                onDrawBehind {
                    drawPath(path, Cream)
                    clipPath(path) {
                        cracks.forEach { (a, b) -> drawLine(Ink.copy(alpha = 0.28f), a, b, 1f * d) }
                        specks.forEach {
                            drawRect(
                                Ink.copy(alpha = 0.18f),
                                it,
                                Size(1.3f * d, 1.3f * d)
                            )
                        }
                    }
                }
            }
            .padding(start = 12.dp, end = 14.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text.uppercase(),
            color = Ink,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            letterSpacing = 1.5.sp,
            maxLines = 1,
        )
        Spacer(Modifier.width(14.dp))
        Canvas(Modifier.size(width = 8.dp, height = 10.dp)) {
            drawPath(
                Path().apply {
                    moveTo(0f, 0f); lineTo(size.width, size.height / 2); lineTo(
                    0f,
                    size.height
                ); close()
                },
                Ink,
            )
        }
    }
}

private class ArcSeg(val path: Path, val alpha: Float, val frac: Float)
private class Rib(val a: Offset, val b: Offset, val u: Float)
private class Flick(val angle: Float, val len: Float, val jit: List<Float>, val nick: Float)

private fun inkArc(
    c: Offset, rx: Float, ry: Float,
    startDeg: Float, endDeg: Float, d: Float, rnd: Random,
): List<ArcSeg> {
    val steps = ((startDeg - endDeg) / 2f).roundToInt()
    val ph = List(2) { rnd.nextFloat() * 6.28f }
    fun noise(u: Float, f1: Float, f2: Float) =
        0.5f + 0.25f * sin(u * f1 + ph[0]) + 0.25f * sin(u * f2 + ph[1])

    val ribs = List(steps + 1) { i ->
        val u = i / steps.toFloat()
        val r = Math.toRadians((startDeg + (endDeg - startDeg) * u).toDouble())
        val cs = cos(r).toFloat()
        val sn = sin(r).toFloat()
        val n = Offset(ry * cs, rx * sn).let { it / it.getDistance() }
        val taper = min(1f, min(u * 14f, (1f - u) * 9f)).coerceAtLeast(0.15f)
        val w = (1.2f + 2.6f * noise(u, 9f, 23f)) * d * taper
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
    d: Float
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

@Composable
private fun ToastDecor(accent: Color, seed: Int, modifier: Modifier = Modifier) {
    val reveal = remember { Animatable(0f) }
    val fly = remember { Animatable(0f) }
    val pulse = remember { Animatable(1f) }
    val ripple = remember { Animatable(1f) }
    val bob by rememberInfiniteTransition(label = "bob").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(900, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "bobValue",
    )

    LaunchedEffect(Unit) { reveal.animateTo(1f, tween(520, easing = LinearOutSlowInEasing)) }
    LaunchedEffect(Unit) { delay(120); fly.animateTo(1f, tween(420, easing = EaseOutCubic)) }
    LaunchedEffect(Unit) {
        delay(650)
        while (true) {
            launch { ripple.snapTo(0f); ripple.animateTo(1f, tween(700)) }
            pulse.animateTo(1.22f, tween(70)); pulse.animateTo(1f, tween(90))
            pulse.animateTo(1.14f, tween(70)); pulse.animateTo(1f, tween(140))
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
            val rx = 58 * d
            val ry = cardH * 0.62f
            val cx = cardL + 36 * d
            val topY = cardT - R + 3 * d
            val cy = topY + ry
            val sinEnd = ((cardB + 3 * d - cy) / ry).coerceIn(-1f, 1f)
            val startDeg = -90f
            val endDeg = Math.toDegrees(asin(sinEnd.toDouble())).toFloat() - 360f

            val segs = inkArc(Offset(cx, cy), rx, ry, startDeg, endDeg, d, rnd)
            val c = Offset(cx, topY)
            val flicks = listOf(-58f to 15f, -24f to 17f).map { (ang, len) ->
                Flick(
                    ang,
                    len * d,
                    List(12) { rnd.nextFloat() * 2f - 1f },
                    0.35f + rnd.nextFloat() * 0.3f
                )
            }

            onDrawBehind {
                val rv = reveal.value
                segs.forEach { if (it.frac <= rv) drawPath(it.path, Cream.copy(alpha = it.alpha)) }

                val appear = min(1f, rv * 6f)
                if (ripple.value < 1f)
                    drawCircle(
                        accent.copy(alpha = (1f - ripple.value) * 0.6f),
                        R * appear + 12 * d * ripple.value,
                        c,
                        style = Stroke(1.5f * d)
                    )
                val s = pulse.value
                drawCircle(accent, R * s * appear, c)
                drawCircle(InkDeep, (R - 3.8f * d) * s * appear, c)
                drawCircle(Color(0xFFF3ECDD), 4.6f * d * (1f + (s - 1f) * 1.8f) * appear, c)

                val f = fly.value
                flicks.forEach { fl ->
                    val r = Math.toRadians(fl.angle.toDouble())
                    val dir = Offset(cos(r).toFloat(), sin(r).toFloat())
                    val from = c + dir * (R + (2f + 7f * f) * d)
                    drawFlick(
                        from,
                        dir,
                        fl.len * f + 2f * d,
                        3.2f * d,
                        Cream.copy(alpha = min(1f, f * 2.5f)),
                        fl.jit,
                        fl.nick,
                        d
                    )
                }

                val tx = cardL + (cardR - cardL) / 2f
                val ty = cardB + (8f + bob * 3f) * d
                drawPath(
                    Path().apply {
                        moveTo(tx - 6 * d, ty); lineTo(tx + 6 * d, ty); lineTo(
                        tx,
                        ty + 7 * d
                    ); close()
                    },
                    Cream.copy(alpha = 0.9f),
                )
            }
        },
    )
}

private fun List<Offset>.toPath(): Path = Path().apply {
    moveTo(first().x, first().y)
    for (i in 1 until size) lineTo(this@toPath[i].x, this@toPath[i].y)
    close()
}

private fun roughRectPoints(
    r: Rect,
    step: Float,
    jitter: Float,
    tear: Float,
    rnd: Random
): List<Offset> {
    val out = ArrayList<Offset>()
    fun edge(a: Offset, b: Offset, inward: Offset) {
        val len = (b - a).getDistance()
        val n = max(1, (len / step).roundToInt())
        for (i in 0 until n) {
            val p = a + (b - a) * (i / n.toFloat())
            val dd = (rnd.nextFloat() * 1.4f - 0.3f) * jitter
            out.add(p + inward * dd)
            if (rnd.nextFloat() < tear) {
                val depth = jitter * (2f + rnd.nextFloat() * 3f)
                val along = (b - a) * (0.35f / n)
                out.add(p + along + inward * depth)
                out.add(p + along * 2.2f + inward * (depth * 0.2f))
            }
        }
    }
    edge(r.topLeft, r.topRight, Offset(0f, 1f))
    edge(r.topRight, r.bottomRight, Offset(-1f, 0f))
    edge(r.bottomRight, r.bottomLeft, Offset(0f, -1f))
    edge(r.bottomLeft, r.topLeft, Offset(1f, 0f))
    return out
}

private fun filmSkillLabel(skill: String, seed: Int): String {
    val random = Random(seed + 91)

    return if (random.nextFloat() < 0.015f) {
        "$skill     ▶"
    } else {
        skill
    }
}

@Composable
fun EggToastPlayground(
    text: String,
    modifier: Modifier = Modifier,
) {
    val toast = remember {
        EasterEggs.ActiveToast(
            id = 1L,
            eggId = "playground",
            reply = EggReply(
                skill = PredefinedSpeakers.LOGIC,
                text = text,
                difficulty = Difficulty.Medium,
                result = CheckResult.Success,
            ),
            durationMillis = Long.MAX_VALUE,
        )
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        EggToast(
            toast = toast,
            onDismiss = {},
        )
    }
}
