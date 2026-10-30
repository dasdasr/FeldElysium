package com.feldelysium.app.presentation.playback.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.time.Duration.Companion.milliseconds

internal class ColumnScrollAnchor(private val scope: CoroutineScope, private val onScrollActive: (Boolean) -> Unit) {
    val textShift = Animatable(0f)
    val tapeShift = Animatable(0f)

    val scrollFraction: Float
        get() = if (maxBack > 1f) 1f - userOffset / maxBack else 1f

    private var bonus = 0f
    private var tapeTarget = 0f
    private var anchoredHeight = 0f
    private var pinnedTop = 0f
    private var hasAnchor = false
    private var generation = 0
    private var job: Job? = null

    val rollShift = Animatable(0f)
    private var rollTarget = 0f

    val footerLift = Animatable(0f)
    private var liftTarget = 0f
    private var maxBack = 0f

    var userOffset by mutableFloatStateOf(0f)
        private set

    fun roll(distancePx: Float) {
        rollTarget += distancePx
        val target = rollTarget

        scope.launch {
            delay(
                CheckMotion.TAPE_ROLL_START_DELAY_MS.toLong().milliseconds,
            )
            rollShift.animateTo(
                targetValue = target,
                animationSpec = tween(
                    durationMillis = CheckMotion.TAPE_ROLL_MS,
                    easing = CheckMotion.TAPE_ROLL_EASING,
                ),
            )
        }
    }

    fun updateFooterLift(overflowPx: Float, hasFooter: Boolean) {
        val target = if (hasFooter) {
            max(liftTarget, overflowPx)
        } else {
            0f
        }

        if (abs(target - liftTarget) < 1f) return

        liftTarget = target

        scope.launch {
            footerLift.animateTo(
                targetValue = target,
                animationSpec = tween(
                    durationMillis = PlaybackMotion.FOOTER_LIFT_MS,
                    easing = FastOutSlowInEasing,
                ),
            )
        }
    }

    fun noteDragBounds(finalY: Float) {
        maxBack = (-(finalY - liftTarget)).coerceAtLeast(0f)
        userOffset = userOffset.coerceIn(0f, maxBack)
    }

    fun dragBy(delta: Float) {
        userOffset = (userOffset + delta).coerceIn(0f, maxBack)
    }

    suspend fun settleUser() {
        if (userOffset == 0f) return

        animate(
            initialValue = userOffset,
            targetValue = 0f,
            animationSpec = tween(150),
        ) { value, _ ->
            userOffset = value
        }
    }

    fun placementY(contentHeightPx: Float, restBottomPx: Float, viewportHeightPx: Float, textShiftPx: Float): Float {
        if (!hasAnchor) {
            if (contentHeightPx < 1f || viewportHeightPx < 1f) {
                return restBottomPx
            }

            hasAnchor = true
            anchoredHeight = contentHeightPx
            pinnedTop = restBottomPx - contentHeightPx
        } else if (contentHeightPx > anchoredHeight + 1f) {
            val heightIncrease = contentHeightPx - anchoredHeight
            anchoredHeight = contentHeightPx

            val scrollDelta = scrollDeltaForHeightIncrease(
                policy = PlaybackMotion.scrollPolicy,
                heightIncreasePx = heightIncrease,
                pinnedTopPx = pinnedTop,
                contentHeightPx = contentHeightPx,
                viewportHeightPx = viewportHeightPx,
                settledScrollPx = bonus,
            )

            if (scrollDelta > 1f) {
                bonus += scrollDelta
                tapeTarget += scrollDelta *
                    PlaybackMotion.TAPE_SCROLL_DISTANCE_FACTOR
                startScroll()
            }
        }

        val appliedShift = textShiftPx.coerceIn(0f, bonus)

        return when (PlaybackMotion.scrollPolicy) {
            PlaybackScrollPolicy.ReturnToRestAnchor ->
                restBottomPx - contentHeightPx + (bonus - appliedShift)

            PlaybackScrollPolicy.ScrollOnlyWhenExceedsScreen ->
                pinnedTop - appliedShift
        }
    }

    private fun startScroll() {
        val gen = ++generation
        job?.cancel()

        job = scope.launch {
            textShift.stop()
            tapeShift.stop()

            delay(PlaybackMotion.SCROLL_START_DELAY_MS.milliseconds)

            if (gen != generation) return@launch

            val textTarget = bonus
            val tapeDestination = tapeTarget

            onScrollActive(true)

            try {
                coroutineScope {
                    launch {
                        textShift.animateTo(
                            targetValue = textTarget,
                            animationSpec = tween(
                                durationMillis = PlaybackMotion.COLUMN_SCROLL_DURATION_MS,
                                easing = PlaybackMotion.columnScrollEasing,
                            ),
                        )
                    }

                    launch {
                        tapeShift.animateTo(
                            targetValue = tapeDestination,
                            animationSpec = tween(
                                durationMillis = PlaybackMotion.TAPE_SCROLL_DURATION_MS,
                                easing = PlaybackMotion.tapeScrollEasing,
                            ),
                        )
                    }
                }
            } finally {
                if (gen == generation) {
                    onScrollActive(false)
                }
            }
        }
    }
}
