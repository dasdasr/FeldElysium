package com.example.feldelysium.domain.usecase

import com.example.feldelysium.domain.model.DialogueEntry
import com.example.feldelysium.domain.model.Speaker
import com.example.feldelysium.domain.registry.PredefinedSpeakers
import java.util.UUID

/**
 * Parses freeform text blocks in the format:
 *
 *   VOLITION:
 *   This city is trying to kill you.
 *
 *   KIM KITSURAGI:
 *   Let's focus on the case.
 *
 * Rules:
 *   - Speaker header: all-caps words ending with colon, alone on a line.
 *   - Text: all lines after the header until the next header or blank line.
 *   - Only predefined speakers are matched by default; custom speakers can be passed in.
 *   - Unrecognized headers are skipped; their text is discarded.
 *   - Results contain entries in order of appearance.
 */
class ParseBulkImportUseCase {

    data class ParseResult(
        val entries: List<DialogueEntry>,
        val unrecognizedSpeakers: List<String>   // for user-facing warnings
    )

    fun execute(rawText: String, customSpeakers: List<Speaker> = emptyList()): ParseResult {
        // Build a merged lookup: predefined + any session custom speakers
        val nameIndex: Map<String, Speaker> = buildMap {
            PredefinedSpeakers.ALL.forEach { put(it.name.uppercase(), it) }
            customSpeakers.forEach { put(it.name.uppercase(), it) }
        }

        val entries = mutableListOf<DialogueEntry>()
        val unrecognized = linkedSetOf<String>()   // preserves order, deduplicates

        var currentSpeaker: Speaker? = null
        val textBuffer = StringBuilder()

        fun flushEntry() {
            val text = textBuffer.toString().trim()
            if (text.isNotEmpty() && currentSpeaker != null) {
                entries += DialogueEntry(
                    id      = UUID.randomUUID().toString(),
                    speaker = currentSpeaker!!,
                    text    = text
                )
            }
            textBuffer.clear()
        }

        for (line in rawText.lines()) {
            val match = SPEAKER_HEADER.matchEntire(line.trim())
            if (match != null) {
                flushEntry()
                val rawName = match.groupValues[1].trim()
                val speaker = nameIndex[rawName.uppercase()]
                if (speaker != null) {
                    currentSpeaker = speaker
                } else {
                    unrecognized += rawName
                    currentSpeaker = null   // discard lines until next recognized header
                }
            } else if (currentSpeaker != null) {
                if (textBuffer.isNotEmpty()) textBuffer.append('\n')
                textBuffer.append(line)
            }
        }
        flushEntry()

        return ParseResult(
            entries              = entries,
            unrecognizedSpeakers = unrecognized.toList()
        )
    }

    companion object {
        // Matches "VOLITION:", "KIM KITSURAGI:", "HAND/EYE COORDINATION:", etc.
        // Anchored to full line. Requires at least one letter, allows slashes and hyphens.
        private val SPEAKER_HEADER = Regex("""^([A-Z][A-Z0-9 /\-]+):$""")
    }
}