package com.example.feldelysium.domain.repository

import com.example.feldelysium.domain.model.ImportPolicy
import com.example.feldelysium.domain.model.ImportPreview
import com.example.feldelysium.domain.model.ImportReport
import com.example.feldelysium.domain.model.PreparedPack
import com.example.feldelysium.domain.model.Speaker
import com.example.feldelysium.domain.model.SpeakerType
import kotlinx.coroutines.flow.Flow
import java.io.InputStream
import java.io.OutputStream

interface SpeakerRepository {
    val speakers: Flow<List<Speaker>>
    val tags: Flow<List<SpeakerType>>

    suspend fun get(id: String): Speaker?
    suspend fun save(speaker: Speaker)
    suspend fun delete(id: String)

    suspend fun saveTag(tag: SpeakerType)
    suspend fun deleteTag(id: String): Int

    suspend fun pruneIcons()

    suspend fun exportPack(ids: Set<String>?, output: OutputStream)
    suspend fun readPack(input: InputStream): PreparedPack
    suspend fun previewImport(pack: PreparedPack): ImportPreview
    suspend fun importPack(pack: PreparedPack, policy: ImportPolicy): ImportReport
}
