package com.example.feldelysium.domain.usecase

import com.example.feldelysium.domain.model.ImportPolicy
import com.example.feldelysium.domain.model.ImportPreview
import com.example.feldelysium.domain.model.ImportReport
import com.example.feldelysium.domain.model.PreparedPack
import com.example.feldelysium.domain.repository.SpeakerRepository
import java.io.InputStream

class ImportSpeakerPackUseCase(private val repo: SpeakerRepository) {

    class Prepared(val pack: PreparedPack, val preview: ImportPreview) {
        val needsDecision: Boolean get() = preview.conflicts.isNotEmpty()
    }

    suspend fun prepare(input: InputStream): Prepared {
        val pack = repo.readPack(input)
        return try {
            Prepared(pack, repo.previewImport(pack))
        } catch (e: Exception) {
            pack.discard(); throw e
        }
    }

    suspend fun apply(prepared: Prepared, policy: ImportPolicy = ImportPolicy.SKIP): ImportReport =
        repo.importPack(prepared.pack, policy)

    fun cancel(prepared: Prepared) = prepared.pack.discard()
}
