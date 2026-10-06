package com.example.feldelysium.presentation.playback.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
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
import com.example.feldelysium.presentation.playback.PlaybackMotion
import com.example.feldelysium.presentation.playback.PlaybackScrollPolicy
import com.example.feldelysium.presentation.playback.scrollDeltaForHeightIncrease
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Film texture plus the dialogue column.
 *
 * New content is measured into the column immediately, so it hangs below the
 * current bottom. [ColumnScrollAnchor] then moves every line by the same
 * distance until that bottom sits on [PlaybackMotion.REST_BOTTOM_FRACTION].
 * The texture moves only during that scroll, by [PlaybackMotion.TAPE_SCROLL_DISTANCE_FACTOR].
 *
 * [footer] scrolls with the lines. Pass CONTINUE or choices here — the rest
 * position is the bottom of whatever is last, footer included. The gap above
 * a CONTINUE block is not added automatically; use [PlaybackMotion.CONTINUE_GAP_FRACTION].
 */
@Composable
fun DialogueHistoryList(
    entries: List<DialogueEntry>,
    modifier: Modifier = Modifier,
    footer: (@Composable () -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()
    val anchor = remember { ColumnScrollAnchor(scope) }
    val textShiftPx = anchor.textShift.value
    val tapeOffsetPx = anchor.tapeShift.value

    BoxWithConstraints(modifier = modifier.clipToBounds()) {
        if (!maxWidth.value.isFinite() || !maxHeight.value.isFinite()) return@BoxWithConstraints

        val density = LocalDensity.current
        val viewportPx = with(density) { maxHeight.toPx() }
        val restBottomPx = viewportPx * PlaybackMotion.REST_BOTTOM_FRACTION
        val textStart = maxWidth * PlaybackMotion.TEXT_COLUMN_START_FRACTION
        val textEndInset = maxWidth * (1f - PlaybackMotion.TEXT_COLUMN_END_FRACTION)
        val bodyLineHeight = MaterialTheme.typography.bodyMedium.lineHeight
        val entryGap = with(density) {
            (bodyLineHeight.toPx() * PlaybackMotion.ENTRY_GAP_LINE_HEIGHTS).toDp()
        }

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
                )
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
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(14.dp),
        )
    }
}

/**
 * One upward shift, in pixels, shared by the column and (scaled) by the tape.
 *
 * [bonus] jumps with each new block in the same layout pass, so existing lines
 * do not hop. [textShift] then animates up to [bonus], which is the scroll.
 */
private class ColumnScrollAnchor(
    private val scope: CoroutineScope,
) {
    val textShift = Animatable(0f)
    val tapeShift = Animatable(0f)

    var scrollFraction: Float? = null
        private set

    private var bonus = 0f
    private var tapeTarget = 0f
    private var anchoredHeight = 0f
    private var pinnedTop = 0f
    private var hasAnchor = false
    private var generation = 0
    private var job: kotlinx.coroutines.Job? = null

    fun placementY(
        contentHeightPx: Float,
        restBottomPx: Float,
        viewportHeightPx: Float,
        textShiftPx: Float,
    ): Float {
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

    fun noteScroll(
        y: Float,
        contentHeightPx: Float,
        restBottomPx: Float,
    ) {
        val maxOffset = (contentHeightPx - restBottomPx).coerceAtLeast(0f)
        scrollFraction = if (maxOffset > 1f) {
            ((-y).coerceAtLeast(0f) / maxOffset).coerceIn(0f, 1f)
        } else {
            null
        }
    }

    private fun startScroll() {
        val gen = ++generation
        job?.cancel()
        job = scope.launch {
            textShift.stop()
            tapeShift.stop()
            delay(PlaybackMotion.SCROLL_START_DELAY_MS.toLong())
            if (gen != generation) return@launch
            val textTarget = bonus
            val tapeDestination = tapeTarget
            coroutineScope {
                launch {
                    textShift.animateTo(
                        textTarget,
                        tween(
                            durationMillis = PlaybackMotion.COLUMN_SCROLL_DURATION_MS,
                            easing = PlaybackMotion.columnScrollEasing,
                        ),
                    )
                }
                launch {
                    tapeShift.animateTo(
                        tapeDestination,
                        tween(
                            durationMillis = PlaybackMotion.TAPE_SCROLL_DURATION_MS,
                            easing = PlaybackMotion.tapeScrollEasing,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun TapeScrollIndicator(
    anchor: ColumnScrollAnchor,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier) {
        val fraction = anchor.scrollFraction ?: return@Canvas
        val lineX = size.width - 3.dp.toPx()
        val lineInset = 16.dp.toPx()
        val lineTop = lineInset
        val lineBottom = size.height - lineInset
        if (lineBottom <= lineTop) return@Canvas
        drawLine(
            color = Color.White.copy(alpha = 0.38f),
            start = Offset(lineX, lineTop),
            end = Offset(lineX, lineBottom),
            strokeWidth = 1.dp.toPx(),
        )
        val dotRadius = 2.25.dp.toPx()
        val dotInset = 10.dp.toPx() + dotRadius
        val travelTop = lineTop + dotInset
        val travelBottom = lineBottom - dotInset
        val dotY = if (travelBottom <= travelTop) {
            (lineTop + lineBottom) / 2f
        } else {
            travelTop + (travelBottom - travelTop) * fraction
        }
        drawCircle(
            color = Color.White,
            radius = dotRadius,
            center = Offset(lineX, dotY),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun DialogueHistoryListPreview() {
    DialogueHistoryList(
        entries = listOf(
            DialogueEntry(speaker = PredefinedSpeakers.STEBAN, text = "Go-go gyro, we are so back, so so bak, you know?"),
            DialogueEntry(speaker = PredefinedSpeakers.STEBAN, text = "Go-go gyro"),
            DialogueEntry(speaker = PredefinedSpeakers.STEBAN, text = "Go-go gyro"),
        ),
        modifier = Modifier
            .width(280.dp)
            .height(480.dp),
    )
}
