package com.example.feldelysium.presentation.playback.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.example.feldelysium.domain.model.DialogueEntry
import com.example.feldelysium.domain.registry.PredefinedSpeakers
import kotlin.math.roundToInt

private const val TOP_FADE_FRACTION = 0.18f

@Composable
fun DialogueHistoryList(
    entries: List<DialogueEntry>,
    modifier: Modifier = Modifier,
    dragToScroll: Boolean = false,
    isCheckRolling: Boolean = false,
    onScrollActive: (Boolean) -> Unit = {},
    footer: (@Composable (FooterMetrics) -> Unit)? = null,
) {
    BoxWithConstraints(modifier = modifier.clipToBounds()) {
        if (!maxWidth.value.isFinite() || !maxHeight.value.isFinite()) {
            return@BoxWithConstraints
        }

        val density = LocalDensity.current
        val viewportPx = with(density) { maxHeight.toPx() }

        val scope = rememberCoroutineScope()
        val currentOnScrollActive by rememberUpdatedState(onScrollActive)
        val anchor = remember {
            ColumnScrollAnchor(scope) { currentOnScrollActive(it) }
        }

        LaunchedEffect(isCheckRolling) {
            if (isCheckRolling) {
                anchor.roll(viewportPx * CheckMotion.TAPE_ROLL_VIEWPORTS)
            }
        }

        val restBottomPx = viewportPx * PlaybackMotion.REST_BOTTOM_FRACTION
        val safeBottomPx = viewportPx * PlaybackMotion.FOOTER_SAFE_BOTTOM_FRACTION

        val textStart = maxWidth * PlaybackMotion.TEXT_COLUMN_START_FRACTION
        val textEndInset = maxWidth * (1f - PlaybackMotion.TEXT_COLUMN_END_FRACTION)

        val fontSize = with(density) {
            (maxHeight * PlaybackMotion.FONT_SIZE_FRACTION).toSp()
        }
        val lineHeight = with(density) {
            (maxHeight * PlaybackMotion.LINE_HEIGHT_FRACTION).toSp()
        }
        val entryGap = maxHeight * (
            PlaybackMotion.LINE_HEIGHT_FRACTION *
                PlaybackMotion.ENTRY_GAP_LINE_HEIGHTS
            )

        val metrics = FooterMetrics(
            fontSize = fontSize,
            lineHeight = lineHeight,
            buttonHeight = maxHeight * PlaybackMotion.CONTINUE_HEIGHT_FRACTION,
        )
        val footerGapPx = viewportPx * PlaybackMotion.CONTINUE_GAP_FRACTION

        LaunchedEffect(dragToScroll, entries.size) {
            anchor.settleUser()
        }

        val dragState = rememberDraggableState { delta ->
            anchor.dragBy(delta)
        }

        TapeBackground(
            offsetPx = {
                anchor.tapeShift.value +
                    anchor.rollShift.value -
                    anchor.userOffset
            },
            modifier = Modifier.fillMaxSize(),
        )

        Layout(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.verticalGradient(
                            0f to Color.Transparent,
                            TOP_FADE_FRACTION to Color.Black,
                        ),
                        blendMode = BlendMode.DstIn,
                    )
                }
                .draggable(
                    state = dragState,
                    orientation = Orientation.Vertical,
                    enabled = dragToScroll && !isCheckRolling,
                ),
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = textStart,
                            end = textEndInset,
                        ),
                    verticalArrangement = Arrangement.spacedBy(entryGap),
                ) {
                    entries.forEachIndexed { index, entry ->
                        key(entry.id) {
                            DialogueEntryItem(
                                entry = entry,
                                isLatest = index == entries.lastIndex,
                                dimmed = isCheckRolling && index == entries.lastIndex,
                                fontSize = fontSize,
                                lineHeight = lineHeight,
                            )
                        }
                    }
                }

                if (footer != null) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                start = textStart,
                                end = textEndInset,
                            ),
                    ) {
                        footer(metrics)
                    }
                }
            },
        ) { measurables, constraints ->
            val fixed = Constraints(
                minWidth = constraints.maxWidth,
                maxWidth = constraints.maxWidth,
                minHeight = 0,
                maxHeight = Constraints.Infinity,
            )

            val placeable = measurables[0].measure(fixed)
            val footerPlaceable = measurables.getOrNull(1)?.measure(fixed)
            val footerHeight = footerPlaceable?.height ?: 0

            val contentHeight = placeable.height.toFloat()

            val baseY = anchor.placementY(
                contentHeightPx = contentHeight,
                restBottomPx = restBottomPx,
                viewportHeightPx = viewportPx,
                textShiftPx = 0f,
            )

            val finalY = anchor.placementY(
                contentHeightPx = contentHeight,
                restBottomPx = restBottomPx,
                viewportHeightPx = viewportPx,
                textShiftPx = Float.MAX_VALUE,
            )

            val overflow = if (footerHeight > 0) {
                (
                    finalY +
                        contentHeight +
                        footerGapPx +
                        footerHeight -
                        safeBottomPx
                    ).coerceAtLeast(0f)
            } else {
                0f
            }

            anchor.updateFooterLift(
                overflowPx = overflow,
                hasFooter = footerHeight > 0,
            )
            anchor.noteDragBounds(finalY)

            layout(
                width = constraints.maxWidth,
                height = constraints.maxHeight,
            ) {
                val y = anchor.placementY(
                    contentHeightPx = contentHeight,
                    restBottomPx = restBottomPx,
                    viewportHeightPx = viewportPx,
                    textShiftPx = anchor.textShift.value,
                ) - anchor.footerLift.value + anchor.userOffset

                placeable.place(
                    x = 0,
                    y = y.roundToInt(),
                )

                footerPlaceable?.place(
                    x = 0,
                    y = (y + placeable.height + footerGapPx).roundToInt(),
                )
            }
        }

        TapeScrollIndicator(
            anchor = anchor,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

private fun DrawScope.drawChevron(x: Float, tipY: Float, up: Boolean) {
    val w = 4.dp.toPx()
    val h = 3.dp.toPx()
    val legY = if (up) tipY + h else tipY - h
    val path = Path().apply {
        moveTo(x - w, legY)
        quadraticTo(x - w / 2f, tipY, x, tipY)
        quadraticTo(x + w / 2f, tipY, x + w, legY)
    }
    drawPath(
        path = path,
        color = Color.White.copy(alpha = 0.35f),
        style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
    )
}

@Composable
private fun TapeScrollIndicator(anchor: ColumnScrollAnchor, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val lineX = size.width * (
            PlaybackMotion.SCROLLBAR_X_FRACTION -
                PlaybackMotion.SCROLLBAR_X_SHIFT_FRACTION
            )

        val lineTop = size.height * PlaybackMotion.SCROLLBAR_TOP_FRACTION
        val lineBottom = size.height * PlaybackMotion.SCROLLBAR_BOTTOM_FRACTION

        if (lineBottom <= lineTop) return@Canvas

        drawLine(
            color = Color.White.copy(alpha = 0.35f),
            start = Offset(lineX, lineTop),
            end = Offset(lineX, lineBottom),
            strokeWidth = 1.dp.toPx(),
        )

        drawChevron(lineX, lineTop - 2.dp.toPx(), up = true)
        drawChevron(lineX, lineBottom + 2.dp.toPx(), up = false)

        val lineLength = lineBottom - lineTop
        val travelTop = lineTop +
            lineLength * PlaybackMotion.SCROLLBAR_DOT_RANGE_START
        val travelBottom = lineTop +
            lineLength * PlaybackMotion.SCROLLBAR_DOT_RANGE_END

        val dotY = travelTop +
            (travelBottom - travelTop) * anchor.scrollFraction

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
                speaker = PredefinedSpeakers.LEGO_HARRY,
                text = "Go-go gyro, we are so back, so so bak, you know?",
            ),
            DialogueEntry(
                speaker = PredefinedSpeakers.LEGO_KIM,
                text = "Go-go gyro",
            ),
            DialogueEntry(
                speaker = PredefinedSpeakers.YOU,
                text = "Go-go gyro",
            ),
        ),
        modifier = Modifier
            .width(280.dp)
            .height(480.dp),
    )
}
