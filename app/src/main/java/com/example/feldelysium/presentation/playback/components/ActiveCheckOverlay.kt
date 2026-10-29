package com.example.feldelysium.presentation.playback.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import com.example.feldelysium.domain.model.SkillCheck
import com.example.feldelysium.presentation.playback.CheckOverlay
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun ActiveCheckOverlay(overlay: CheckOverlay?, modifier: Modifier = Modifier) {
    if (overlay == null) return
    key(overlay.entryId) { ActiveCheckOverlayContent(overlay.check, modifier) }
}

@Composable
private fun ActiveCheckOverlayContent(check: SkillCheck, modifier: Modifier) {
    val background = remember { Animatable(1f) }
    val dice = remember { Animatable(1f) }
    val textOut = remember { Animatable(0f) } // 0 = in place, 1 = gone

    LaunchedEffect(Unit) {
        launch {
            delay(CheckMotion.BG_FADE_DELAY_MS.milliseconds)
            background.animateTo(0f, tween(CheckMotion.BG_FADE_MS, easing = EaseIn))
        }
        launch {
            delay(CheckMotion.DICE_FADE_DELAY_MS.milliseconds)
            dice.animateTo(0f, tween(CheckMotion.DICE_FADE_MS, easing = EaseIn))
        }
        launch {
            delay(CheckMotion.TEXT_OUT_DELAY_MS.milliseconds)
            textOut.animateTo(1f, tween(CheckMotion.TEXT_OUT_MS, easing = EaseIn))
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val diceSize = maxHeight * CheckMotion.DICE_SIZE_FRACTION
        val diceGap = maxHeight * CheckMotion.DICE_GAP_FRACTION
        val textHeight = maxHeight * CheckMotion.RESULT_TEXT_HEIGHT_FRACTION
        val slidePx = with(density) { maxWidth.toPx() } * CheckMotion.TEXT_SLIDE_FRACTION
        val (die1, die2) = check.dice

        BlendedImage(
            res = CheckAssets.background(check.result),
            modifier = Modifier.matchParentSize(),
            blendMode = CheckMotion.BG_BLEND,
            bottomFit = true,
            alpha = { background.value },
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = maxHeight * CheckMotion.OVERLAY_BOTTOM_FRACTION),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.graphicsLayer { alpha = dice.value },
                horizontalArrangement = Arrangement.spacedBy(diceGap),
            ) {
                PipDie(die1, Modifier.size(diceSize))
                PipDie(die2, Modifier.size(diceSize))
            }
            ResultPlaque(
                text = CheckAssets.label(check.result),
                color = CheckAssets.resultColor(check.result),
                height = textHeight,
                modifier = Modifier.graphicsLayer {
                    alpha = 1f - textOut.value
                    translationX = textOut.value * slidePx
                },
            )
        }
    }
}

// Hollow die face (so the background shows through): rounded outline + pips, drawn on a 96x96 grid.
private const val GRID = 96f
private const val NEAR = 28f
private const val MID = 48f
private const val FAR = 68f
private const val TOP = 24f
private const val BOTTOM = 72f

private fun pips(value: Int): List<Pair<Float, Float>> = when (value) {
    1 -> listOf(MID to MID)

    2 -> listOf(NEAR to NEAR, FAR to FAR)

    3 -> listOf(NEAR to NEAR, MID to MID, FAR to FAR)

    4 -> listOf(NEAR to NEAR, FAR to NEAR, NEAR to FAR, FAR to FAR)

    5 -> listOf(NEAR to NEAR, FAR to NEAR, MID to MID, NEAR to FAR, FAR to FAR)

    else -> listOf(
        NEAR to TOP,
        NEAR to MID,
        NEAR to BOTTOM,
        FAR to TOP,
        FAR to MID,
        FAR to BOTTOM,
    )
}

@Composable
fun PipDie(value: Int, modifier: Modifier = Modifier, color: Color = Color.White) {
    Canvas(modifier) {
        val u = size.width / GRID
        drawRoundRect(
            color = color,
            topLeft = Offset(2.5f * u, 2.5f * u),
            size = Size(91f * u, 91f * u),
            cornerRadius = CornerRadius(13.5f * u),
            style = Stroke(width = 5f * u),
        )
        pips(value.coerceIn(1, 6)).forEach { (x, y) ->
            drawCircle(color = color, radius = 8f * u, center = Offset(x * u, y * u))
        }
    }
}
