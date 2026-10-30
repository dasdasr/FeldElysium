package com.feldelysium.app.domain.scene

import com.feldelysium.app.domain.model.DialogueEntry
import com.feldelysium.app.domain.model.DialogueScene
import com.feldelysium.app.domain.model.ImportPolicy
import com.feldelysium.app.domain.model.ImportPreview
import com.feldelysium.app.domain.model.ImportReport
import com.feldelysium.app.domain.model.PreparedPack
import com.feldelysium.app.domain.model.Speaker
import com.feldelysium.app.domain.model.SpeakerType
import com.feldelysium.app.domain.repository.SpeakerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.InputStream
import java.io.OutputStream

class SceneSpeakerReconcilerTest {

    @Test
    fun knownSpeakerIsReplacedWithTheLiveCopy() = runTest {
        val live = Speaker("kim", "Kim Kitsuragi", 0xFF112233, type = SpeakerType.NPC)
        val stored = live.copy(name = "Kim", colorArgb = 1L)
        val repo = FakeSpeakerRepository(listOf(live))
        val scene = DialogueScene(
            entries = listOf(DialogueEntry(speaker = stored, text = "Hello")),
        )

        val reconciled = SceneSpeakerReconciler(repo).reconcile(scene)

        assertEquals(live, reconciled.entries.single().speaker)
        assertTrue(repo.saved.isEmpty())
    }

    @Test
    fun unknownSpeakerIsSavedAndKeptOnTheEntry() = runTest {
        val stored = Speaker("new", "New", 5L, type = SpeakerType.CUSTOM)
        val repo = FakeSpeakerRepository(emptyList())
        val scene = DialogueScene(
            entries = listOf(
                DialogueEntry(id = "1", speaker = stored, text = "A"),
                DialogueEntry(id = "2", speaker = stored, text = "B"),
            ),
        )

        val reconciled = SceneSpeakerReconciler(repo).reconcile(scene)

        assertEquals(listOf(stored), repo.saved)
        assertEquals(stored, reconciled.entries[0].speaker)
        assertEquals(stored, reconciled.entries[1].speaker)
    }
}

private class FakeSpeakerRepository(initial: List<Speaker>) : SpeakerRepository {
    val saved = mutableListOf<Speaker>()
    private val speakersFlow = MutableStateFlow(initial)
    override val speakers: Flow<List<Speaker>> = speakersFlow
    override val tags = MutableStateFlow<List<SpeakerType>>(emptyList())

    override suspend fun get(id: String): Speaker? = speakersFlow.value.find { it.id == id }

    override suspend fun save(speaker: Speaker) {
        saved += speaker
        speakersFlow.value = speakersFlow.value.filterNot { it.id == speaker.id } + speaker
    }

    override suspend fun delete(id: String) = unsupported()
    override suspend fun saveTag(tag: SpeakerType) = unsupported()
    override suspend fun deleteTag(id: String): Int = unsupported()
    override suspend fun pruneIcons() = unsupported()
    override suspend fun exportPack(ids: Set<String>?, output: OutputStream) = unsupported()
    override suspend fun readPack(input: InputStream): PreparedPack = unsupported()
    override suspend fun previewImport(pack: PreparedPack): ImportPreview = unsupported()
    override suspend fun importPack(pack: PreparedPack, policy: ImportPolicy): ImportReport = unsupported()

    private fun unsupported(): Nothing = error("not used")
}
