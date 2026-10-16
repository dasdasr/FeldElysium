package com.example.feldelysium.presentation.playback.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.example.feldelysium.domain.model.DialogueEntry
import com.example.feldelysium.domain.registry.PredefinedSpeakers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun DialogueHistoryList(
    entries: List<DialogueEntry>,
    modifier: Modifier = Modifier,
    isCheckRolling: Boolean = false,
    onScrollActive: (Boolean) -> Unit = {},
    footer: (@Composable () -> Unit)? = null,
) {
    BoxWithConstraints(modifier = modifier.clipToBounds()) {
        if (!maxWidth.value.isFinite() || !maxHeight.value.isFinite()) return@BoxWithConstraints

        val density = LocalDensity.current
        val viewportPx = with(density) { maxHeight.toPx() }

        val scope = rememberCoroutineScope()
        val currentOnScrollActive by rememberUpdatedState(onScrollActive)
        val anchor = remember { ColumnScrollAnchor(scope) { currentOnScrollActive(it) } }
        val textShiftPx = anchor.textShift.value

        LaunchedEffect(isCheckRolling) {
            if (isCheckRolling) anchor.roll(viewportPx * CheckMotion.TAPE_ROLL_VIEWPORTS)
        }

        val restBottomPx = viewportPx * PlaybackMotion.REST_BOTTOM_FRACTION
        val textStart = maxWidth * PlaybackMotion.TEXT_COLUMN_START_FRACTION
        val textEndInset = maxWidth * (1f - PlaybackMotion.TEXT_COLUMN_END_FRACTION)
        val fontSize = with(density) { (maxHeight * PlaybackMotion.FONT_SIZE_FRACTION).toSp() }
        val lineHeight = with(density) { (maxHeight * PlaybackMotion.LINE_HEIGHT_FRACTION).toSp() }
        val entryGap = maxHeight * (PlaybackMotion.LINE_HEIGHT_FRACTION * PlaybackMotion.ENTRY_GAP_LINE_HEIGHTS)
        val tapeOffsetPx = anchor.tapeShift.value + anchor.rollShift.value

        TapeBackground(
            offsetPx = tapeOffsetPx,
            modifier = Modifier.fillMaxSize(),
        )

        Layout(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds(),
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = textStart, end = textEndInset),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(entryGap),
                    ) {
                        entries.forEachIndexed { index, entry ->
                            key(entry.id) {
                                DialogueEntryItem(
                                    entry = entry,
                                    isLatest = index == entries.lastIndex,
                                    fontSize = fontSize,
                                    lineHeight = lineHeight,
                                )
                            }
                        }
                    }
                    footer?.invoke()
                }
            },
        ) { measurables, constraints ->
            val placeable = measurables.single().measure(
                Constraints(
                    minWidth = constraints.maxWidth,
                    maxWidth = constraints.maxWidth,
                    minHeight = 0,
                    maxHeight = Constraints.Infinity,
                ),
            )
            val y = anchor.placementY(
                contentHeightPx = placeable.height.toFloat(),
                restBottomPx = restBottomPx,
                viewportHeightPx = viewportPx,
                textShiftPx = textShiftPx,
            )
            anchor.noteScroll(
                y = y,
                contentHeightPx = placeable.height.toFloat(),
                restBottomPx = restBottomPx,
            )
            layout(constraints.maxWidth, constraints.maxHeight) {
                placeable.place(0, y.roundToInt())
            }
        }

        TapeScrollIndicator(
            anchor = anchor,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

// Shared scroll offset for the dialogue column and tape background
private class ColumnScrollAnchor(private val scope: CoroutineScope, private val onScrollActive: (Boolean) -> Unit) {
    val textShift = Animatable(0f)
    val tapeShift = Animatable(0f)

    var scrollFraction: Float = 0f
        private set

    private var bonus = 0f
    private var tapeTarget = 0f
    private var anchoredHeight = 0f
    private var pinnedTop = 0f
    private var hasAnchor = false
    private var generation = 0
    private var job: kotlinx.coroutines.Job? = null

    val rollShift = Animatable(0f)
    private var rollTarget = 0f

    fun roll(distancePx: Float) {
        rollTarget += distancePx
        val target = rollTarget
        scope.launch {
            delay(CheckMotion.TAPE_ROLL_START_DELAY_MS.toLong().milliseconds)
            rollShift.animateTo(target, tween(CheckMotion.TAPE_ROLL_MS, easing = EaseInOut))
        }
    }

    fun placementY(contentHeightPx: Float, restBottomPx: Float, viewportHeightPx: Float, textShiftPx: Float): Float {
        if (!hasAnchor) {
            if (contentHeightPx < 1f || viewportHeightPx < 1f) return restBottomPx
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
                tapeTarget += scrollDelta * PlaybackMotion.TAPE_SCROLL_DISTANCE_FACTOR
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

    fun noteScroll(y: Float, contentHeightPx: Float, restBottomPx: Float) {
        val maxOffset = (contentHeightPx - restBottomPx).coerceAtLeast(0f)
        scrollFraction = if (maxOffset > 1f) {
            ((-y).coerceAtLeast(0f) / maxOffset).coerceIn(0f, 1f)
        } else {
            0f
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
            coroutineScope {
                launch {
                    textShift.animateTo(
                        textTarget,
                        tween(PlaybackMotion.COLUMN_SCROLL_DURATION_MS, easing = PlaybackMotion.columnScrollEasing),
                    )
                }
                launch {
                    tapeShift.animateTo(
                        tapeDestination,
                        tween(PlaybackMotion.TAPE_SCROLL_DURATION_MS, easing = PlaybackMotion.tapeScrollEasing),
                    )
                }
            }
            onScrollActive(false)
        }
    }
}

@Composable
private fun TapeScrollIndicator(anchor: ColumnScrollAnchor, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val lineX = size.width * (
            PlaybackMotion.SCROLLBAR_X_FRACTION - PlaybackMotion.SCROLLBAR_X_SHIFT_FRACTION
            )
        val lineTop = size.height * PlaybackMotion.SCROLLBAR_TOP_FRACTION
        val lineBottom = size.height * PlaybackMotion.SCROLLBAR_BOTTOM_FRACTION
        if (lineBottom <= lineTop) return@Canvas
        drawLine(
            color = Color.White.copy(alpha = 0.38f),
            start = Offset(lineX, lineTop),
            end = Offset(lineX, lineBottom),
            strokeWidth = 1.dp.toPx(),
        )
        val lineLength = lineBottom - lineTop
        val travelTop = lineTop + lineLength * PlaybackMotion.SCROLLBAR_DOT_RANGE_START
        val travelBottom = lineTop + lineLength * PlaybackMotion.SCROLLBAR_DOT_RANGE_END
        val dotY = travelTop + (travelBottom - travelTop) * anchor.scrollFraction
        drawCircle(
            color = Color.White,
            radius = 3.dp.toPx(),
            center = Offset(lineX, dotY),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun DialogueHistoryListPreview() {
    DialogueHistoryList(
        entries = listOf(
            DialogueEntry(
                speaker = PredefinedSpeakers.STEBAN,
                text = "Go-go gyro, we are so back, so so bak, you know?",
            ),
            DialogueEntry(speaker = PredefinedSpeakers.STEBAN, text = "Go-go gyro"),
            DialogueEntry(speaker = PredefinedSpeakers.STEBAN, text = "Go-go gyro"),
        ),
        modifier = Modifier
            .width(280.dp)
            .height(480.dp),
    )
}
