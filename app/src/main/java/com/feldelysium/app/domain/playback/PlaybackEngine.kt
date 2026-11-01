package com.feldelysium.app.domain.playback

import com.feldelysium.app.domain.model.DialogueEntry

class PlaybackEngine(private val entries: List<DialogueEntry>) {

    private var cursor: Int = 0

    val isFinished: Boolean get() = cursor >= entries.size
    val hasEntries: Boolean get() = entries.isNotEmpty()

    fun advance(): PlaybackStep? {
        if (isFinished) return null
        val entry = entries[cursor]
        cursor++
        return PlaybackStep(
            entry = entry,
            revealedCount = cursor,
            isLast = cursor >= entries.size,
        )
    }

    fun reset() {
        cursor = 0
    }

    fun peek(): DialogueEntry? = entries.getOrNull(cursor)

    fun revealedEntries(): List<DialogueEntry> = entries.subList(0, cursor)
}

data class PlaybackStep(val entry: DialogueEntry, val revealedCount: Int, val isLast: Boolean)
