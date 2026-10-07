package com.example.feldelysium.domain.usecase

import com.example.feldelysium.domain.model.DialogueEntry
import com.example.feldelysium.domain.model.Speaker
import com.example.feldelysium.domain.registry.PredefinedSpeakers
import java.util.UUID

class ParseBulkImportUseCase {

    data class ParseResult(
        val entries: List<DialogueEntry>,
        val unrecognizedSpeakers: List<String>
    )

    fun execute(rawText: String, customSpeakers: List<Speaker> = emptyList()): ParseResult {
        val nameIndex: Map<String, Speaker> = buildMap {
            PredefinedSpeakers.ALL.forEach { put(it.name.uppercase(), it) }
            customSpeakers.forEach { put(it.name.uppercase(), it) }
        }

        val entries = mutableListOf<DialogueEntry>()
        val unrecognized = linkedSetOf<String>()

        var currentSpeaker: Speaker? = null
        val textBuffer = StringBuilder()

        fun flushEntry() {
            val text = textBuffer.toString().trim()
            if (text.isNotEmpty() && currentSpeaker != null) {
                entries += DialogueEntry(
                    id = UUID.randomUUID().toString(),
                    speaker = currentSpeaker!!,
                    text = text
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
                    currentSpeaker = null
                }
            } else if (currentSpeaker != null) {
                if (textBuffer.isNotEmpty()) textBuffer.append('\n')
                textBuffer.append(line)
            }
        }

        flushEntry()

        return ParseResult(
            entries = entries,
            unrecognizedSpeakers = unrecognized.toList()
        )
    }

    companion object {
        // Speaker name must be uppercase and occupy the whole line.
        private val SPEAKER_HEADER = Regex("""^([A-Z][A-Z0-9 /\-]+):$""")
    }
}
