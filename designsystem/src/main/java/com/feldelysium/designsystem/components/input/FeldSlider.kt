package com.feldelysium.designsystem.components.input

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.feldelysium.designsystem.foundation.material.accentBrush
import com.feldelysium.designsystem.foundation.material.agsl.SLIDER_TRACK_AGSL
import com.feldelysium.designsystem.foundation.seed.rememberSeed
import com.feldelysium.designsystem.theme.FeldPalette
import com.feldelysium.designsystem.theme.FeldTheme
import kotlin.math.abs

public enum class FeldThumb { Diamond, Rectangle, Circle }

private val OffColor = Color(0xFF1F2322)
private val ThumbRim = Color(0xFF3A3128)

private fun FeldThumb.size(): DpSize = when (this) {
    FeldThumb.Diamond -> DpSize(26.dp, 26.dp)
    FeldThumb.Rectangle -> DpSize(12.dp, 26.dp)
    FeldThumb.Circle -> DpSize(22.dp, 22.dp)
}

@Composable
public fun FeldSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    accent: Color = FeldAccentColors.Cream,
    thumb: FeldThumb = FeldThumb.Rectangle,
    label: String? = null,
    valueText: ((Float) -> String)? = null,
    trackHeight: Dp = 10.dp,
    seed: Int = rememberSeed(),
    onValueChangeFinished: (() -> Unit)? = null,
) {
    val density = LocalDensity.current
    val currentOnChange by rememberUpdatedState(onValueChange)
    val currentFinished by rememberUpdatedState(onValueChangeFinished)

    var widthPx by remember { mutableIntStateOf(0) }
    var pressed by remember { mutableStateOf(false) }
    val press by animateFloatAsState(if (pressed) 1f else 0f, tween(110), label = "press")
    val pressState = rememberUpdatedState(press)

    val span = valueRange.endInclusive - valueRange.start
    val fraction = ((value - valueRange.start) / span).coerceIn(0f, 1f)
    val fractionState = rememberUpdatedState(fraction)

    val thumbDp = thumb.size()
    val thumbW = with(density) { thumbDp.width.toPx() }

    Column(modifier.fillMaxWidth()) {
        if (label != null || valueText != null) {
            Row(
                Modifier.fillMaxWidth().padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text((label ?: "").uppercase(), style = MaterialTheme.typography.labelMedium, color = FeldPalette.Muted)
                if (valueText != null) {
                    Text(valueText(value), style = MaterialTheme.typography.labelMedium, color = FeldPalette.Cream)
                }
            }
        }

        Box(
            Modifier
                .fillMaxWidth()
                .height(thumbDp.height + 8.dp)
                .onSizeChanged { widthPx = it.width }
                .semantics {
                    progressBarRangeInfo = ProgressBarRangeInfo(value, valueRange)
                    setProgress { v ->
                        onValueChange(v.coerceIn(valueRange))
                        true
                    }
                }
                .pointerInput(valueRange, thumbW) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        pressed = true
                        var drag = false

                        fun setFromX(x: Float) {
                            val usable = (widthPx - thumbW).coerceAtLeast(1f)
                            val f = ((x - thumbW / 2f) / usable).coerceIn(0f, 1f)
                            currentOnChange(valueRange.start + f * span)
                        }

                        while (true) {
                            val ev = awaitPointerEvent()
                            val c = ev.changes.firstOrNull { it.id == down.id } ?: break
                            if (c.isConsumed && !drag) break
                            if (!c.pressed) {
                                if (!drag) setFromX(c.position.x)
                                break
                            }
                            if (!drag && abs(c.position.x - down.position.x) > viewConfiguration.touchSlop) drag = true
                            if (drag) {
                                setFromX(c.position.x)
                                c.consume()
                            }
                        }
                        pressed = false
                        currentFinished?.invoke()
                    }
                },
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(trackHeight)
                    .sliderTrack(accent, OffColor, seed, thumbW) { fractionState.value },
            )

            Box(
                Modifier
                    .size(thumbDp)
                    .graphicsLayer {
                        translationX = fractionState.value * (widthPx - thumbW).coerceAtLeast(0f)
                        val s = 1f + 0.12f * pressState.value
                        scaleX = s
                        scaleY = s
                    }
                    .sliderThumb(thumb, seed + 5) { pressState.value },
            )
        }
    }
}

private fun Modifier.sliderTrack(fill: Color, off: Color, seed: Int, thumbW: Float, fraction: () -> Float): Modifier =
    drawWithCache {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val shader = RuntimeShader(SLIDER_TRACK_AGSL).apply {
                setFloatUniform("size", size.width, size.height)
                setFloatUniform("seed", seed.mod(97).toFloat())
                setFloatUniform("u", density)
                setFloatUniform("fadeW", 18.dp.toPx())
                setColorUniform("fillColor", fill.toArgb())
                setColorUniform("offColor", off.toArgb())
            }
            val brush = ShaderBrush(shader)
            onDrawWithContent {
                shader.setFloatUniform("fillX", thumbW / 2f + fraction() * (size.width - thumbW))
                drawRect(brush)
            }
        } else {
            onDrawWithContent {
                val r = CornerRadius(size.height / 2f)
                drawRoundRect(off, cornerRadius = r)
                val x = thumbW / 2f + fraction() * (size.width - thumbW)
                clipRect(right = x) { drawRoundRect(fill, cornerRadius = r) }
            }
        }
    }

private fun Modifier.sliderThumb(kind: FeldThumb, seed: Int, press: () -> Float): Modifier = drawWithCache {
    val w = size.width
    val h = size.height
    val path = Path().apply {
        when (kind) {
            FeldThumb.Diamond -> {
                moveTo(w / 2f, 0f)
                lineTo(w, h / 2f)
                lineTo(w / 2f, h)
                lineTo(0f, h / 2f)
                close()
            }

            FeldThumb.Circle -> addOval(androidx.compose.ui.geometry.Rect(0f, 0f, w, h))

            FeldThumb.Rectangle -> addRoundRect(RoundRect(0f, 0f, w, h, CornerRadius(3.dp.toPx())))
        }
    }
    val brush = accentBrush(FeldPalette.Cream, seed, density, fade = 0.55f, glow = 0.7f, grain = 0.06f)
    val rim = Stroke(0.8.dp.toPx(), join = StrokeJoin.Round)
    val unit = 1.dp.toPx()

    onDrawWithContent {
        val p = press()
        for (i in 1..3) {
            val dy = (1f + i * 1.1f + 2f * p) * unit
            withTransform({
                translate(0f, dy)
                scale(1f + 0.04f * i, 1f + 0.04f * i)
            }) { drawPath(path, Color.Black, alpha = 0.13f) }
        }
        drawPath(path, brush)
        drawPath(path, ThumbRim, alpha = 0.5f, style = rim)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1011, widthDp = 360)
@Composable
private fun FeldSliderPreview() {
    FeldTheme {
        var a by remember { mutableFloatStateOf(0.7f) }
        var b by remember { mutableFloatStateOf(0.45f) }
        var c by remember { mutableFloatStateOf(0.8f) }
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            FeldSlider(
                a,
                { a = it },
                accent = FeldAccentColors.Teal,
                label = "Volume",
                valueText = { "${(it * 100).toInt()}%" },
            )
            FeldSlider(
                b,
                { b = it },
                accent = FeldAccentColors.Orange,
                label = "Brightness",
                valueText = { "${(it * 100).toInt()}%" },
            )
            FeldSlider(
                c,
                { c = it },
                accent = FeldAccentColors.Cream,
                label = "Progress",
                thumb = FeldThumb.Circle,
            )
        }
    }
}
