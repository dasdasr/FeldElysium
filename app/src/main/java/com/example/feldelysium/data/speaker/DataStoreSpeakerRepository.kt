package com.example.feldelysium.data.speaker

import android.content.Context
import androidx.datastore.core.DataStore
import com.example.feldelysium.domain.model.ImportPolicy
import com.example.feldelysium.domain.model.ImportPreview
import com.example.feldelysium.domain.model.ImportReport
import com.example.feldelysium.domain.model.PreparedPack
import com.example.feldelysium.domain.model.Speaker
import com.example.feldelysium.domain.model.SpeakerColor
import com.example.feldelysium.domain.model.SpeakerData
import com.example.feldelysium.domain.model.SpeakerDocument
import com.example.feldelysium.domain.model.SpeakerRef
import com.example.feldelysium.domain.model.SpeakerType
import com.example.feldelysium.domain.model.TagData
import com.example.feldelysium.domain.registry.PredefinedSpeakers
import com.example.feldelysium.domain.repository.SpeakerRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class DataStoreSpeakerRepository(
    context: Context,
    private val icons: SpeakerIconStore,
    private val store: DataStore<SpeakerDocument>,
    private val io: CoroutineDispatcher = Dispatchers.IO,
) : SpeakerRepository {

    private val appContext = context.applicationContext
    private val bundledIds = PredefinedSpeakers.ALL.map { it.id }.toSet()

    override val tags: Flow<List<SpeakerType>> = store.data
        .map { doc ->
            PredefinedSpeakers.TAGS +
                    doc.tags.filterNot { PredefinedSpeakers.isBundledTag(it.id) }
                        .map { it.toType() }
        }
        .distinctUntilChanged()

    override val speakers: Flow<List<Speaker>> = store.data
        .map { doc ->
            val tagMap =
                (doc.tags.map { it.toType() } + PredefinedSpeakers.TAGS).associateBy { it.id }
            PredefinedSpeakers.ALL +
                    doc.speakers.filter { it.id !in bundledIds }.map { it.toSpeaker(tagMap) }
        }
        .distinctUntilChanged()

    override suspend fun get(id: String): Speaker? = speakers.first().firstOrNull { it.id == id }

    override suspend fun save(speaker: Speaker) {
        if (speaker.id in bundledIds) return
        val data = speaker.toData()
        val newTag = speaker.type
            .takeUnless { it.isCustom || PredefinedSpeakers.isBundledTag(it.id) }
            ?.toData()
        store.updateData { doc ->
            val tags =
                if (newTag != null && doc.tags.none { it.id == newTag.id }) doc.tags + newTag else doc.tags
            doc.copy(tags = tags, speakers = doc.speakers.replaceOrAdd(data) { it.id == data.id })
        }
        pruneIcons()
    }

    override suspend fun delete(id: String) {
        store.updateData { doc -> doc.copy(speakers = doc.speakers.filterNot { it.id == id }) }
        pruneIcons()
    }

    override suspend fun saveTag(tag: SpeakerType) {
        if (tag.isCustom || PredefinedSpeakers.isBundledTag(tag.id)) return
        val data = tag.toData()
        store.updateData { doc -> doc.copy(tags = doc.tags.replaceOrAdd(data) { it.id == data.id }) }
    }

    override suspend fun deleteTag(id: String): Int {
        if (PredefinedSpeakers.isBundledTag(id)) return 0
        var affected = 0
        store.updateData { doc ->
            affected = doc.speakers.count { it.tagId == id }
            doc.copy(
                tags = doc.tags.filterNot { it.id == id },
                speakers = doc.speakers.map { if (it.tagId == id) it.copy(tagId = null) else it },
            )
        }
        return affected
    }

    override suspend fun pruneIcons() = withContext(io) {
        val referenced = store.data.first().speakers
            .mapNotNull { it.icon?.takeIf { r -> r.startsWith("file:") }?.removePrefix("file:") }
            .toSet()
        icons.prune(referenced)
    }

    override suspend fun exportPack(ids: Set<String>?, output: OutputStream) = withContext(io) {
        val doc = store.data.first()
        val chosen = doc.speakers.filter { ids == null || it.id in ids } +
                PredefinedSpeakers.ALL.filter { ids != null && it.id in ids }.map { it.toData() }

        val tagPool = (doc.tags + PredefinedSpeakers.TAGS.map { it.toData() }).associateBy { it.id }
        val usedTags = chosen.mapNotNull { it.tagId }.distinct().mapNotNull { tagPool[it] }

        val files = chosen
            .mapNotNull { it.icon?.takeIf { r -> r.startsWith("file:") }?.removePrefix("file:") }
            .distinct().associateWith { icons.fileOf(it) }

        SpeakerPackIO.write(SpeakerDocument(usedTags, chosen), files, output)
    }

    override suspend fun readPack(input: InputStream): PreparedPack = withContext(io) {
        val temp = File(appContext.cacheDir, "pack_import_${UUID.randomUUID()}").apply { mkdirs() }
        try {
            PreparedPack(SpeakerPackIO.read(input, temp), temp)
        } catch (e: Exception) {
            temp.deleteRecursively(); throw e
        }
    }

    override suspend fun previewImport(pack: PreparedPack): ImportPreview {
        val doc = store.data.first()
        val existing = doc.speakers.map { it.id }.toSet()
        val tagIds = doc.tags.map { it.id }.toSet() + PredefinedSpeakers.TAGS.map { it.id }
        val incoming = pack.document

        return ImportPreview(
            newSpeakers = incoming.speakers.count { it.id !in existing && it.id !in bundledIds },
            conflicts = incoming.speakers.filter { it.id in existing }
                .map { SpeakerRef(it.id, it.name) },
            bundledSkipped = incoming.speakers.count { it.id in bundledIds },
            newTags = incoming.tags.count { it.id !in tagIds },
            existingTags = incoming.tags.filter { it.id in tagIds }.map { it.name },
        )
    }

    override suspend fun importPack(pack: PreparedPack, policy: ImportPolicy): ImportReport =
        withContext(io) {
            var added = 0;
            var replaced = 0;
            var skipped = 0;
            var tagsAdded = 0;
            var tagsUpdated = 0
            try {
                store.updateData { doc ->
                    added = 0; replaced = 0; skipped = 0; tagsAdded = 0; tagsUpdated = 0

                    val tags = doc.tags.toMutableList()
                    pack.document.tags.forEach { t ->
                        if (PredefinedSpeakers.isBundledTag(t.id)) return@forEach
                        val i = tags.indexOfFirst { it.id == t.id }
                        when {
                            i < 0 -> {
                                tags += t; tagsAdded++
                            }

                            policy == ImportPolicy.REPLACE -> {
                                tags[i] = t; tagsUpdated++
                            }
                        }
                    }
                    fun tagColor(id: String?): Long? = id?.let { tid ->
                        tags.firstOrNull { it.id == tid }?.color
                            ?: PredefinedSpeakers.TAGS.firstOrNull { it.id == tid }?.colorArgb
                    }

                    fun tagKnown(id: String) =
                        tags.any { it.id == id } || PredefinedSpeakers.isBundledTag(id)

                    val speakers = doc.speakers.toMutableList()
                    pack.document.speakers.forEach { s ->
                        if (s.id in bundledIds) {
                            skipped++; return@forEach
                        }
                        val idx = speakers.indexOfFirst { it.id == s.id }
                        if (idx >= 0 && policy == ImportPolicy.SKIP) {
                            skipped++; return@forEach
                        }

                        s.tagId?.let { tid ->
                            if (!tagKnown(tid)) {
                                tags += TagData(
                                    tid,
                                    tid.replaceFirstChar { it.uppercase() }
                                        .take(SpeakerType.MAX_NAME)
                                )
                                tagsAdded++
                            }
                        }
                        val newId =
                            if (idx >= 0 && policy == ImportPolicy.KEEP_BOTH) UUID.randomUUID()
                                .toString() else s.id
                        val color = SpeakerColor.snap(
                            s.color ?: tagColor(s.tagId) ?: SpeakerColor.DEFAULT.argb
                        )
                        val data =
                            SpeakerData(newId, s.name, color, s.tagId, adoptIcon(s.icon, pack))

                        if (idx >= 0 && policy == ImportPolicy.REPLACE) {
                            speakers[idx] = data; replaced++
                        } else {
                            speakers += data; added++
                        }
                    }
                    doc.copy(tags = tags, speakers = speakers)
                }
            } finally {
                pack.discard()
            }
            pruneIcons()
            ImportReport(added, replaced, skipped, tagsAdded, tagsUpdated)
        }

    private fun adoptIcon(ref: String?, pack: PreparedPack): String? = when {
        ref == null -> null
        ref.startsWith("res:") -> ref.takeIf { resId(ref.removePrefix("res:")) != null }
        ref.startsWith("file:") -> {
            val src = File(pack.dir, "icons/" + File(ref.removePrefix("file:")).name)
            if (src.exists()) "file:" + icons.adopt(src) else null
        }

        else -> null
    }

    private fun resId(name: String): Int? =
        appContext.resources.getIdentifier(name, "drawable", appContext.packageName)
            .takeIf { it != 0 }

    private fun resName(id: Int): String? =
        runCatching { appContext.resources.getResourceEntryName(id) }.getOrNull()

    private fun Speaker.toData() = SpeakerData(
        id = id, name = name, color = colorArgb,
        tagId = type.id.takeUnless { type.isCustom },
        icon = customIconUri?.let { icons.nameOf(it) }?.let { "file:$it" }
            ?: iconResId?.let { resName(it) }?.let { "res:$it" },
    )

    private fun SpeakerData.toSpeaker(tagMap: Map<String, SpeakerType>): Speaker {
        val ref = icon
        return Speaker(
            id = id, name = name,
            colorArgb = color ?: SpeakerColor.DEFAULT.argb,
            iconResId = ref?.takeIf { it.startsWith("res:") }
                ?.let { resId(it.removePrefix("res:")) },
            customIconUri = ref?.takeIf { it.startsWith("file:") }
                ?.let { icons.uriOf(it.removePrefix("file:")) },
            type = tagId?.let { tagMap[it] } ?: SpeakerType.CUSTOM,
        )
    }

    private fun TagData.toType() = SpeakerType(id, name, color, sound)
    private fun SpeakerType.toData() = TagData(id, name, colorArgb, soundKey)

    private inline fun <T> List<T>.replaceOrAdd(item: T, match: (T) -> Boolean): List<T> {
        val i = indexOfFirst(match)
        return if (i < 0) this + item else toMutableList().also { it[i] = item }
    }
}
