package com.example.feldelysium.domain.usecase

import com.example.feldelysium.domain.model.DialogueEntry
import com.example.feldelysium.domain.model.Speaker
import com.example.feldelysium.domain.model.SpeakerType
import com.example.feldelysium.domain.registry.PredefinedSpeakers
import java.util.Locale
import java.util.UUID

class ParseBulkImportUseCase {

    data class ParseResult(val entries: List<DialogueEntry>, val newSpeakers: List<Speaker>)

    fun execute(rawText: String, customSpeakers: List<Speaker> = emptyList()): ParseResult {
        val nameIndex: Map<String, Speaker> = buildMap {
            PredefinedSpeakers.ALL.forEach { put(it.name.uppercase(Locale.ROOT), it) }
            customSpeakers.forEach { put(it.name.uppercase(Locale.ROOT), it) }
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
                if (currentIsNew) newSpeakers[speaker.name.uppercase(Locale.ROOT)] = speaker
                entries += DialogueEntry(
                    id = UUID.randomUUID().toString(),
                    speaker = speaker,
                    text = text,
                )
            }
            textBuffer.clear()
        }

        for (line in rawText.lines()) {
            val match = SPEAKER_LINE.matchEntire(line)?.takeIf {
                val name = it.groupValues[1].trim()
                nameIndex.containsKey(name.uppercase(Locale.ROOT)) || name == name.uppercase(Locale.ROOT)
            }
            if (match != null) {
                flushEntry()
                val rawName = match.groupValues[1].trim()
                val key = rawName.uppercase(Locale.ROOT)
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
                val inline = match.groupValues[2].trim()
                if (inline.isNotEmpty()) textBuffer.append(inline)
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
        val SPEAKER_LINE = Regex("""^\s*([\p{L}][\p{L}\p{N} /-]*):\s*(.*)$""")
    }
}
