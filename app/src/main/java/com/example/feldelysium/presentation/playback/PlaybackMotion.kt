package com.example.feldelysium.presentation.playback

import androidx.compose.animation.core.Easing

/**
 * Tunables for the playback column and film texture.
 *
 * Durations, easing, fade, and the dimmed alpha are placeholders.
 * They are not measured from the game — change them against a screen recording.
 *
 * [TAPE_SCROLL_DISTANCE_FACTOR] is how many texture pixels move per pixel of
 * column scroll. An unverified guess was about 2; the default keeps them equal.
 */
object PlaybackMotion {
    /** Bottom of the latest block (line, choices, or CONTINUE) at rest. Fraction of screen height. */
    const val REST_BOTTOM_FRACTION = 0.67f

    /** Text column inside the tape, as fractions of tape width (side strip included). Right edge is at [TEXT_COLUMN_END_FRACTION]. */
    const val TEXT_COLUMN_START_FRACTION = 0.105f
    const val TEXT_COLUMN_END_FRACTION = 0.88f

    /** Baseline-to-baseline distance inside an entry, as a fraction of scene height. */
    const val LINE_HEIGHT_FRACTION = 0.033f

    /**
     * Body size as a fraction of scene height.
     * Same 12:16 ratio as the previous sp pair, so the size shrinks with [LINE_HEIGHT_FRACTION].
     */
    const val FONT_SIZE_FRACTION = LINE_HEIGHT_FRACTION * 12f / 16f

    /**
     * Extra space between entries, in body line-heights.
     * Baseline of the last line to baseline of the next entry's first line is
     * `1 + ENTRY_GAP_LINE_HEIGHTS` line intervals (the line box already contributes one).
     */
    const val ENTRY_GAP_LINE_HEIGHTS = 0.6f

    /** Scrollbar line on the tape. Fractions of tape width and tape height. */
    const val SCROLLBAR_X_FRACTION = 0.94f

    /** Extra shift of the scrollbar to the left, fraction of tape width. */
    const val SCROLLBAR_X_SHIFT_FRACTION = 0.025f

    const val SCROLLBAR_TOP_FRACTION = 0.22f
    const val SCROLLBAR_BOTTOM_FRACTION = 0.78f

    /** Dot travel along the scrollbar line, as fractions of the line length. */
    const val SCROLLBAR_DOT_RANGE_START = 0.2f
    const val SCROLLBAR_DOT_RANGE_END = 0.8f

    const val COLUMN_SCROLL_DURATION_MS = 200
    const val TAPE_SCROLL_DURATION_MS = 200
    const val SCROLL_START_DELAY_MS = 40

    /**
     * Film yank: constant speed from the first frame, then a short hard stop.
     * [SCROLL_YANK_FRACTION] is how much of the tween is the yank.
     * [SCROLL_YANK_DISTANCE] is how far the column has moved when braking starts.
     */
    const val SCROLL_YANK_FRACTION = 0.68f
    const val SCROLL_YANK_DISTANCE = 0.92f

    val columnScrollEasing: Easing = filmYankEasing
    val tapeScrollEasing: Easing = filmYankEasing

    const val TEXT_FADE_IN_MS = 120
    const val PREVIOUS_DIM_MS = 150

    /** Older lines, as a fraction of the latest line's brightness. */
    const val PREVIOUS_LINES_ALPHA = 0.5f

    const val TAPE_SCROLL_DISTANCE_FACTOR = 1f

    /** Measured CONTINUE chrome, for when that block is passed as the column footer. */
    const val CONTINUE_GAP_FRACTION = 0.03f
    const val CONTINUE_HEIGHT_FRACTION = 0.05f

    /**
     * [ReturnToRestAnchor] matches the short-dialogue captures: the new line is laid out
     * under the previous ones, then the whole column moves up until its bottom is back
     * at [REST_BOTTOM_FRACTION].
     *
     * [ScrollOnlyWhenExceedsScreen] is the unverified "no scroll while it fits" idea.
     * Lines stack downward from the first line. Scroll starts only once the bottom
     * would leave the screen, and only by that overflow — not back to 67%.
     */
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
