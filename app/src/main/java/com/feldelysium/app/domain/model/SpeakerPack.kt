package com.feldelysium.app.domain.model

import java.io.File

enum class ImportPolicy { SKIP, REPLACE, KEEP_BOTH }

data class SpeakerRef(val id: String, val name: String)

data class ImportPreview(
    val newSpeakers: Int,
    val conflicts: List<SpeakerRef>,
    val bundledSkipped: Int,
    val newTags: Int,
    val existingTags: List<String>,
)

data class ImportReport(val added: Int, val replaced: Int, val skipped: Int, val tagsAdded: Int, val tagsUpdated: Int)

class PreparedPack(val document: SpeakerDocument, val dir: File) {
    fun discard() {
        dir.deleteRecursively()
    }
}
