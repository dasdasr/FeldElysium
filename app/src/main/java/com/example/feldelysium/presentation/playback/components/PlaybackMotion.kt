package com.example.feldelysium.presentation.playback.components

import androidx.compose.animation.core.Easing

object PlaybackMotion {
    const val REST_BOTTOM_FRACTION = 0.67f

    const val TEXT_COLUMN_START_FRACTION = 0.105f
    const val TEXT_COLUMN_END_FRACTION = 0.88f

    const val LINE_HEIGHT_FRACTION = 0.033f
    const val FONT_SIZE_FRACTION = LINE_HEIGHT_FRACTION * 12f / 16f

    const val ENTRY_GAP_LINE_HEIGHTS = 0.6f

    const val SCROLLBAR_X_FRACTION = 0.94f
    const val SCROLLBAR_X_SHIFT_FRACTION = 0.025f
    const val SCROLLBAR_TOP_FRACTION = 0.22f
    const val SCROLLBAR_BOTTOM_FRACTION = 0.78f

    const val SCROLLBAR_DOT_RANGE_START = 0.2f
    const val SCROLLBAR_DOT_RANGE_END = 0.8f

    const val COLUMN_SCROLL_DURATION_MS = 200
    const val TAPE_SCROLL_DURATION_MS = 200
    const val SCROLL_START_DELAY_MS = 40

    // Quick movement followed by a hard stop
    const val SCROLL_YANK_FRACTION = 0.68f
    const val SCROLL_YANK_DISTANCE = 0.92f

    val columnScrollEasing: Easing = filmYankEasing
    val tapeScrollEasing: Easing = filmYankEasing

    const val TEXT_FADE_IN_MS = 120
    const val PREVIOUS_DIM_MS = 150
    const val PREVIOUS_LINES_ALPHA = 0.5f

    const val TAPE_SCROLL_DISTANCE_FACTOR = 1f

    const val CONTINUE_GAP_FRACTION = 0.03f
    const val CONTINUE_HEIGHT_FRACTION = 0.05f

    val scrollPolicy: PlaybackScrollPolicy = PlaybackScrollPolicy.ReturnToRestAnchor
}

private val filmYankEasing: Easing = Easing { t ->
    val yank = PlaybackMotion.SCROLL_YANK_FRACTION.coerceIn(0.05f, 0.95f)
    val distance = PlaybackMotion.SCROLL_YANK_DISTANCE.coerceIn(0.5f, 0.99f)
    if (t <= yank) {
        (t / yank) * distance
    } else {
        val u = ((t - yank) / (1f - yank)).coerceIn(0f, 1f)
        val brake = 1f - (1f - u) * (1f - u)
        distance + (1f - distance) * brake
    }
}

enum class PlaybackScrollPolicy {
    ReturnToRestAnchor,
    ScrollOnlyWhenExceedsScreen,
}

internal fun scrollDeltaForHeightIncrease(
    policy: PlaybackScrollPolicy,
    heightIncreasePx: Float,
    pinnedTopPx: Float,
    contentHeightPx: Float,
    viewportHeightPx: Float,
    settledScrollPx: Float,
): Float = when (policy) {
    PlaybackScrollPolicy.ReturnToRestAnchor -> heightIncreasePx
    PlaybackScrollPolicy.ScrollOnlyWhenExceedsScreen -> {
        val overflow = (pinnedTopPx + contentHeightPx - viewportHeightPx).coerceAtLeast(0f)
        (overflow - settledScrollPx).coerceAtLeast(0f)
    }
}
