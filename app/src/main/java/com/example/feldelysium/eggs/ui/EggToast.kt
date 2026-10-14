package com.example.feldelysium.eggs.ui

import android.os.SystemClock
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feldelysium.designsystem.components.speaker.FeldSpeakerDiamond
import com.example.feldelysium.designsystem.components.title.FeldTitleTag
import com.example.feldelysium.domain.model.Speaker
import com.example.feldelysium.eggs.EasterEggs
import com.example.feldelysium.eggs.EggReply
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

private fun skillColor(skill: Speaker): Color =
    Color(skill.colorArgb)

private class ToastView(val skill: Speaker, val text: String, val check: String?)

private fun EggReply.toView() = ToastView(
    skill = skill,
    text = text,
    check = difficulty?.let { "[$it: ${result?.label ?: "Success"}]" },
)

@Composable
internal fun EggToast(
    toast: EasterEggs.ActiveToast,
    onDismiss: (Long) -> Unit,
) {
    val view = remember(toast.id) { toast.reply.toView() }
    val accent = skillColor(view.skill)
    val seed = remember(toast.id) { toast.id.toInt() * 31 + view.text.hashCode() }

    val deadline = remember(toast.id) {
        if (toast.durationMillis >= Long.MAX_VALUE / 2) Long.MAX_VALUE
        else SystemClock.elapsedRealtime() + toast.durationMillis
    }
    var held by remember(toast.id) { mutableStateOf(false) }

    LaunchedEffect(toast.id, held) {
        if (held) return@LaunchedEffect
        val left = if (deadline == Long.MAX_VALUE) Long.MAX_VALUE
        else deadline - SystemClock.elapsedRealtime()
        delay(left.coerceAtLeast(ToastReleaseGraceMs).milliseconds)
        onDismiss(toast.id)
    }

    val drag = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val scope = rememberCoroutineScope()
    val returnSpring = spring<Offset>(Spring.DampingRatioLowBouncy, Spring.StiffnessMediumLow)

    Box(
        Modifier
            .widthIn(max = 520.dp)
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .graphicsLayer {
                val raw = drag.value
                val limit = ToastSlack.toPx() * 0.8f
                val up = (-raw.y).coerceAtLeast(0f)
                translationX = raw.x
                translationY = if (raw.y < 0f) -(limit * up / (up + limit)) else raw.y
                rotationZ = raw.x / 60f
                alpha = 1f - (raw.getDistance() / ToastFadeDistance.toPx()).coerceIn(0f, 1f)
                compositingStrategy = CompositingStrategy.ModulateAlpha
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    held = true
                    do {
                        val event = awaitPointerEvent()
                    } while (event.changes.any { it.pressed })
                    held = false
                }
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
                                drag.animateTo(Offset.Zero, returnSpring)
                            }
                        }
                    },
                    onDragCancel = { scope.launch { drag.animateTo(Offset.Zero, returnSpring) } },
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
            Column(
                Modifier.padding(start = 18.dp, end = 20.dp, top = 8.dp, bottom = 6.dp),
            ) {
                Spacer(Modifier.width(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FeldSpeakerDiamond(
                        color = accent,
                        size = 22.dp,
                        seed = seed,
                    )
                    FeldTitleTag(
                        text = filmSkillLabel(view.skill.name, seed),
                        seed = seed,
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 20.sp,
                            letterSpacing = 0.4.sp,
                        ),
                    )
                }
                Spacer(Modifier.height(4.dp))
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
                )
            }
        }
    }
}

internal fun List<Offset>.toPath(): Path = Path().apply {
    moveTo(first().x, first().y)
    for (i in 1 until size) lineTo(this@toPath[i].x, this@toPath[i].y)
    close()
}

internal fun chamferRectPoints(
    r: Rect,
    cutTL: Float, cutTR: Float, cutBR: Float, cutBL: Float,
    step: Float,
    jitter: Float,
    rnd: Random,
): List<Offset> {
    val corners = listOf(
        Offset(r.left + cutTL, r.top), Offset(r.right - cutTR, r.top),
        Offset(r.right, r.top + cutTR), Offset(r.right, r.bottom - cutBR),
        Offset(r.right - cutBR, r.bottom), Offset(r.left + cutBL, r.bottom),
        Offset(r.left, r.bottom - cutBL), Offset(r.left, r.top + cutTL),
    )
    val out = ArrayList<Offset>()
    for (i in corners.indices) {
        val a = corners[i]
        val b = corners[(i + 1) % corners.size]
        val n = max(1, ((b - a).getDistance() / step).roundToInt())
        for (k in 0 until n) {
            val p = a + (b - a) * (k / n.toFloat())
            out.add(p + Offset((rnd.nextFloat() - 0.5f) * jitter, (rnd.nextFloat() - 0.5f) * jitter))
        }
    }
    return out
}

private fun filmSkillLabel(skill: String, seed: Int): String {
    val random = Random(seed + 91)

    return if (random.nextFloat() < 0.15f) {
        "$skill     ▶"
    } else {
        skill
    }
}
