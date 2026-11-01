package com.feldelysium.app.domain.playback

import com.feldelysium.app.domain.model.DialogueEntry
import com.feldelysium.app.domain.model.Speaker
import com.feldelysium.app.domain.model.SpeakerType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackEngineTest {

    private val speaker = Speaker("npc", "Kim", 0L, type = SpeakerType.NPC)

    @Test
    fun advanceRevealsEntriesInOrderAndThenFinishes() {
        val engine = PlaybackEngine(
            listOf(
                DialogueEntry(id = "a", speaker = speaker, text = "One"),
                DialogueEntry(id = "b", speaker = speaker, text = "Two"),
            ),
        )

        val first = engine.advance()
        val second = engine.advance()

        assertEquals("a", first?.entry?.id)
        assertEquals(1, first?.revealedCount)
        assertEquals(false, first?.isLast)
        assertEquals("b", second?.entry?.id)
        assertTrue(second?.isLast == true)
        assertTrue(engine.isFinished)
        assertNull(engine.advance())
        assertEquals(listOf("a", "b"), engine.revealedEntries().map { it.id })
    }

    @Test
    fun resetRestartsFromTheFirstEntry() {
        val engine = PlaybackEngine(listOf(DialogueEntry(id = "a", speaker = speaker, text = "One")))
        engine.advance()

        engine.reset()

        assertFalse(engine.isFinished)
        assertEquals("a", engine.peek()?.id)
        assertEquals("a", engine.advance()?.entry?.id)
    }

    @Test
    fun emptyScriptIsFinishedImmediately() {
        val engine = PlaybackEngine(emptyList())

        assertFalse(engine.hasEntries)
        assertTrue(engine.isFinished)
        assertNull(engine.peek())
        assertNull(engine.advance())
    }
}
