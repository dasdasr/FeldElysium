package com.example.feldelysium.domain.usecase

import com.example.feldelysium.domain.model.Speaker
import com.example.feldelysium.domain.model.SpeakerType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ParseBulkImportUseCaseTest {

    private val useCase = ParseBulkImportUseCase()

    @Test
    fun knownSpeaker_isKept_andCreatesNothing() {
        val result = useCase.execute("VOLITION:\nStay calm.")

        assertEquals(1, result.entries.size)
        assertEquals("volition", result.entries.single().speaker.id)
        assertTrue(result.newSpeakers.isEmpty())
    }

    @Test
    fun unknownSpeaker_becomesCustomSpeaker_withItsLines() {
        val result = useCase.execute("STRANGER:\nWho are you?")

        val speaker = result.newSpeakers.single()
        assertEquals("STRANGER", speaker.name)
        assertEquals(SpeakerType.CUSTOM, speaker.type)
        assertEquals(Speaker.DEFAULT_CUSTOM_COLOR, speaker.colorArgb)
        assertNull(speaker.iconResId)
        assertNull(speaker.customIconUri)
        assertEquals(speaker, result.entries.single().speaker)
        assertEquals("Who are you?", result.entries.single().text)
    }

    @Test
    fun repeatedUnknownName_reusesOneSpeaker() {
        val result = useCase.execute(
            """
            STRANGER:
            First.
            STRANGER:
            Second.
            """.trimIndent()
        )

        assertEquals(1, result.newSpeakers.size)
        assertEquals(2, result.entries.size)
        assertEquals(result.entries[0].speaker.id, result.entries[1].speaker.id)
    }

    @Test
    fun existingCustomSpeaker_isNotDuplicated() {
        val custom = Speaker(
            id = "c1",
            name = "Ancient Reptilian Brain",
            colorArgb = 0xFF_11_22_33L,
            type = SpeakerType.CUSTOM,
        )

        val result = useCase.execute("ANCIENT REPTILIAN BRAIN:\nYes.", listOf(custom))

        assertTrue(result.newSpeakers.isEmpty())
        assertEquals("c1", result.entries.single().speaker.id)
    }

    @Test
    fun emptyBlock_createsNeitherLineNorSpeaker() {
        val result = useCase.execute("GHOST:\n\nVOLITION:\nHi.")

        assertTrue(result.newSpeakers.isEmpty())
        assertEquals(listOf("Hi."), result.entries.map { it.text })
    }

    @Test
    fun blankOrUnstructuredText_yieldsNothing() {
        assertTrue(useCase.execute("   ").entries.isEmpty())
        assertTrue(useCase.execute("just words").entries.isEmpty())
        assertTrue(useCase.execute("just words").newSpeakers.isEmpty())
    }
}
