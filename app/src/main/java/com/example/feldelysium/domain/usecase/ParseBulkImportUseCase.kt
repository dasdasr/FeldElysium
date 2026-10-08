package com.example.feldelysium.domain.usecase

import com.example.feldelysium.domain.model.DialogueEntry
import com.example.feldelysium.domain.model.Speaker
import com.example.feldelysium.domain.model.SpeakerType
import com.example.feldelysium.domain.registry.PredefinedSpeakers
import java.util.UUID

class ParseBulkImportUseCase {

    data class ParseResult(
        val entries: List<DialogueEntry>,
        val newSpeakers: List<Speaker>,
    )

    fun execute(rawText: String, customSpeakers: List<Speaker> = emptyList()): ParseResult {
        val nameIndex: Map<String, Speaker> = buildMap {
            PredefinedSpeakers.ALL.forEach { put(it.name.uppercase(), it) }
            customSpeakers.forEach { put(it.name.uppercase(), it) }
        }

        val entries = mutableListOf<DialogueEntry>()
        val draftedSpeakers = linkedMapOf<String, Speaker>()
        val newSpeakers = linkedMapOf<String, Speaker>()

        var currentSpeaker: Speaker? = null
        var currentIsNew = false
        val textBuffer = StringBuilder()

        fun flushEntry() {
            val text = textBuffer.toString().trim()
            val speaker = currentSpeaker
            if (text.isNotEmpty() && speaker != null) {
                if (currentIsNew) newSpeakers[speaker.name.uppercase()] = speaker
                entries += DialogueEntry(
                    id = UUID.randomUUID().toString(),
                    speaker = speaker,
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
                val key = rawName.uppercase()
                val known = nameIndex[key]
                if (known != null) {
                    currentSpeaker = known
                    currentIsNew = false
                } else {
                    currentSpeaker = draftedSpeakers.getOrPut(key) {
                        Speaker(
                            id = UUID.randomUUID().toString(),
                            name = rawName,
                            colorArgb = Speaker.DEFAULT_CUSTOM_COLOR,
                            type = SpeakerType.CUSTOM,
                        )
                    }
                    currentIsNew = true
                }
            } else if (currentSpeaker != null) {
                if (textBuffer.isNotEmpty()) textBuffer.append('\n')
                textBuffer.append(line)
            }
        }

        flushEntry()

        return ParseResult(
            entries = entries,
            newSpeakers = newSpeakers.values.toList(),
        )
    }

    companion object {
        // Speaker name must be uppercase and occupy the whole line.
        private val SPEAKER_HEADER = Regex("""^([A-Z][A-Z0-9 /\-]+):$""")
    }
}
