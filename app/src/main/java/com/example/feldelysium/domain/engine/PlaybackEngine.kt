package com.example.feldelysium.domain.engine

import com.example.feldelysium.domain.model.DialogueEntry

/**
 * Pure state machine for dialogue progression.
 *
 * Zero Android dependencies. Unit-testable with plain JUnit.
 * Owns only the cursor into the entry list — no UI, no side effects.
 */
class PlaybackEngine(private val entries: List<DialogueEntry>) {

    private var cursor: Int = 0

    val isFinished: Boolean get() = cursor >= entries.size
    val hasEntries: Boolean get() = entries.isNotEmpty()

    /**
     * Advances the cursor and returns the next step, or null if already finished.
     * The step contains enough context for the ViewModel to emit correct effects.
     */
    fun advance(): PlaybackStep? {
        if (isFinished) return null
        val entry = entries[cursor]
        cursor++
        return PlaybackStep(
            entry        = entry,
            revealedCount = cursor,
            isLast       = cursor >= entries.size
        )
    }

    fun reset() { cursor = 0 }

    /** Snapshot of all entries revealed so far, safe to pass directly to UI state. */
    fun revealedEntries(): List<DialogueEntry> = entries.subList(0, cursor)
}

data class PlaybackStep(
    val entry: DialogueEntry,
    val revealedCount: Int,
    val isLast: Boolean
)