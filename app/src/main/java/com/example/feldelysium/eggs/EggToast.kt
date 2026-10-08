package com.example.feldelysium.eggs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

private val PadStart = 30.dp
private val PadTop = 50.dp
private val PadBottom = 26.dp

private fun skillColor(skill: String): Color = when (skill.lowercase()) {
    "logic", "encyclopedia", "rhetoric", "drama", "conceptualization", "visual calculus" -> Color(0xFF4FB3A9)
    "volition", "inland empire", "empathy", "authority", "esprit de corps", "suggestion" -> Color(0xFF8E7FCB)
    "endurance", "pain threshold", "physical instrument", "electrochemistry", "shivers", "half light" -> Color(0xFFC9564B)
    "hand/eye coordination", "perception", "reaction speed", "savoir faire", "interfacing", "composure" -> Color(0xFFD8B54A)
    else -> Color(0xFF4FB3A9)
}

private class ToastView(val skill: String, val text: String, val check: String?)

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

    Box(
        modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        AnimatedVisibility(
            visible = toast != null,
            enter = fadeIn(tween(160)) + slideInVertically(tween(260)) { it / 2 },
            exit = fadeOut(tween(200)) + slideOutVertically(tween(220)) { it / 2 },
        ) {
            current?.let { t ->
                key(t.id) {
                    EggToast(t, eggs::dismiss)
                }
            }
        }
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
            .graphicsLayer {
                translationX = drag.value.x
                translationY = drag.value.y
                rotationZ = drag.value.x / 60f
                alpha = 1f - (drag.value.getDistance() / (220.dp.toPx())).coerceIn(0f, 1f)
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
            Row(Modifier.padding(start = 26.dp, end = 34.dp, top = 12.dp, bottom = 14.dp)) {
                SkillDiamond(accent, seed, Modifier.padding(top = 4.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    ToastTag(view.skill, seed)
                    Spacer(Modifier.height(7.dp))
                    Text(
                        text = buildAnnotatedString {
                            view.check?.let {
                                withStyle(SpanStyle(color = accent, fontWeight = FontWeight.Bold)) { append("$it ") }
                            }
                            append(view.text)
                        },
                        color = TextCream,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 17.sp,
                            lineHeight = 23.sp,
                        ),
                        maxLines = 4,
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
    val band = roughRectPoints(Rect(-3 * d, -2 * d, 15 * d, h + 2 * d), 10 * d, 1.1f * d, 0.08f, rnd).toPath()
    val rightBand = roughRectPoints(Rect(w - 5 * d, 9 * d, w + 0.5f * d, h - 15 * d), 9 * d, 0.8f * d, 0.10f, rnd).toPath()

    val frays = ArrayList<Pair<Offset, Offset>>()
    var x = 8 * d
    while (x < w - 12 * d) {
        val len = (5 + rnd.nextFloat() * 30) * d
        if (rnd.nextFloat() < 0.75f)
            frays.add(Offset(x, 0.8f * d) to Offset(min(x + len, w - 8 * d), 0.8f * d + rnd.nextFloat() * 0.8f * d))
        x += len + (4 + rnd.nextFloat() * 26) * d
    }
    var y = 8 * d
    while (y < h - 14 * d) {
        val len = (5 + rnd.nextFloat() * 24) * d
        if (rnd.nextFloat() < 0.7f)
            frays.add(Offset(w - 0.8f * d, y) to Offset(w - 0.8f * d - rnd.nextFloat() * 0.8f * d, min(y + len, h - 10 * d)))
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

    fun diamond(c: Offset, r: Float) = Path().apply {
        moveTo(c.x, c.y - r * 1.25f); lineTo(c.x + r, c.y); lineTo(c.x, c.y + r * 1.25f); lineTo(c.x - r, c.y); close()
    }

    onDrawBehind {
        drawPath(body, Ink)
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
            bandSpecks.forEach { drawRect(Color.Black.copy(alpha = 0.3f), it, Size(1.4f * d, 1.4f * d)) }
        }
        drawPath(rightBand, accent)

        val dx = w - 17 * d
        drawPath(diamond(Offset(dx, h * 0.26f), 3.5f * d), CreamDim.copy(alpha = 0.7f))
        drawPath(diamond(Offset(dx, h * 0.42f), 4.5f * d), CreamDim.copy(alpha = 0.85f))
        drawPath(diamond(Offset(dx, h * 0.58f), 4.5f * d), CreamDim)
    }
}

@Composable
private fun SkillDiamond(color: Color, seed: Int, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(width = 30.dp, height = 38.dp)
            .drawWithCache {
                val d = density
                val rnd = Random(seed + 7)
                val c = Offset(size.width / 2f, size.height / 2f)
                val rx = size.width / 2 * 0.95f
                val ry = size.height / 2 * 0.95f
                val j = 1.3f * d
                fun p(x: Float, y: Float) =
                    Offset(x + (rnd.nextFloat() - 0.5f) * 2 * j, y + (rnd.nextFloat() - 0.5f) * 2 * j)

                val top = p(c.x, c.y - ry); val right = p(c.x + rx, c.y)
                val bottom = p(c.x, c.y + ry); val left = p(c.x - rx, c.y)
                val mid = { a: Offset, b: Offset -> p((a.x + b.x) / 2, (a.y + b.y) / 2) }
                val path = listOf(top, mid(top, right), right, mid(right, bottom), bottom, mid(bottom, left), left, mid(left, top))
                    .toPath()
                val specks = List(7) { Offset(rnd.nextFloat() * size.width, rnd.nextFloat() * size.height) }

                onDrawBehind {
                    drawPath(path, color)
                    clipPath(path) { specks.forEach { drawRect(Color.Black.copy(alpha = 0.25f), it, Size(1.5f * d, 1.5f * d)) } }
                }
            },
    )
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
                    while (x < w - 10 * d) { add(Offset(x, (rnd.nextFloat() * 1.6f - 0.4f) * d)); x += 12 * d }
                    add(Offset(w, 0f))
                    add(Offset(w - 3 * d, h * 0.45f))
                    add(Offset(w - 9 * d, h))
                    x = w - 20 * d
                    while (x > 8 * d) { add(Offset(x, h - (rnd.nextFloat() * 1.6f - 0.4f) * d)); x -= 12 * d }
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
                        specks.forEach { drawRect(Ink.copy(alpha = 0.18f), it, Size(1.3f * d, 1.3f * d)) }
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
                Path().apply { moveTo(0f, 0f); lineTo(size.width, size.height / 2); lineTo(0f, size.height); close() },
                Ink,
            )
        }
    }
}

private class ArcSeg(val path: Path, val width: Float, val frac: Float, val alpha: Float)

@Composable
private fun ToastDecor(accent: Color, seed: Int, modifier: Modifier = Modifier) {
    val reveal = remember { Animatable(0f) }
    val fly = remember { Animatable(0f) }
    val pulse = remember { Animatable(1f) }
    val ripple = remember { Animatable(1f) }
    val bob by rememberInfiniteTransition(label = "bob").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
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
            val cx = cardL + 40 * d
            val cy = (cardT + cardB) / 2f
            val rx = cx - 6 * d
            val ry = (cardB - cardT) / 2f + 20 * d

            fun pt(deg: Float, dr: Float = 0f): Offset {
                val r = Math.toRadians(deg.toDouble())
                return Offset(cx + (rx + dr) * cos(r).toFloat(), cy + (ry + dr) * sin(r).toFloat())
            }

            val startDeg = -100f
            val endDeg = -275f
            val total = startDeg - endDeg
            val segs = ArrayList<ArcSeg>()
            var a = startDeg
            while (a > endDeg) {
                val sweep = if (rnd.nextFloat() < 0.2f) 2f + rnd.nextFloat() * 3f else 14f + rnd.nextFloat() * 26f
                val a2 = max(a - sweep, endDeg)
                val path = Path()
                var t = a
                var first = true
                while (t >= a2) {
                    val p = pt(t, (rnd.nextFloat() - 0.5f) * 2.4f * d)
                    if (first) { path.moveTo(p.x, p.y); first = false } else path.lineTo(p.x, p.y)
                    t -= 3f
                }
                val e = pt(a2); path.lineTo(e.x, e.y)
                segs.add(ArcSeg(path, (1.6f + rnd.nextFloat() * 1.8f) * d, (startDeg - a) / total, 0.75f + rnd.nextFloat() * 0.25f))
                a = a2 - (3f + rnd.nextFloat() * 8f)
            }

            val c = pt(startDeg)
            val R = 14 * d
            val lineSpecs = listOf(-55f to 14f, -22f to 16f)

            onDrawBehind {
                val rv = reveal.value
                segs.forEach {
                    if (it.frac <= rv)
                        drawPath(it.path, Cream.copy(alpha = it.alpha), style = Stroke(it.width, cap = StrokeCap.Round, join = StrokeJoin.Round))
                }

                val appear = min(1f, rv * 6f)
                if (ripple.value < 1f)
                    drawCircle(accent.copy(alpha = (1f - ripple.value) * 0.6f), R * appear + 14 * d * ripple.value, c, style = Stroke(1.5f * d))
                val s = pulse.value
                drawCircle(accent, R * s * appear, c)
                drawCircle(InkDeep, (R - 4.5f * d) * s * appear, c)
                drawCircle(Color(0xFFF3ECDD), 5.5f * d * (1f + (s - 1f) * 1.8f) * appear, c)

                val f = fly.value
                lineSpecs.forEach { (ang, len) ->
                    val r = Math.toRadians(ang.toDouble())
                    val dir = Offset(cos(r).toFloat(), sin(r).toFloat())
                    val from = c + dir * (R + (2f + 7f * f) * d)
                    val to = from + dir * (len * f * d + 2f * d)
                    drawLine(Cream.copy(alpha = min(1f, f * 2.5f)), from, to, 2f * d, StrokeCap.Round)
                }

                val tx = cardL + (cardR - cardL) / 2f
                val ty = cardB + (8f + bob * 3f) * d
                drawPath(
                    Path().apply {
                        moveTo(tx - 7 * d, ty); lineTo(tx + 7 * d, ty); lineTo(tx, ty + 8 * d); close()
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

private fun roughRectPoints(r: Rect, step: Float, jitter: Float, tear: Float, rnd: Random): List<Offset> {
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

@Composable
fun EggToastPlayground(
    modifier: Modifier = Modifier,
) {
    val toast = remember {
        EasterEggs.ActiveToast(
            id = 3L,
            eggId = "playground",
            reply = EggReply(
                skill = "Logic",
                text = "Something about this place doesn't add up.",
                difficulty = "Medium",
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
