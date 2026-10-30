package com.feldelysium.app.presentation.playback

import app.cash.turbine.test
import com.feldelysium.app.domain.model.DialogueEntry
import com.feldelysium.app.domain.model.DialogueScene
import com.feldelysium.app.domain.model.Speaker
import com.feldelysium.app.domain.model.SpeakerType
import com.feldelysium.app.domain.model.settings.AppSettings
import com.feldelysium.app.domain.playback.PlaybackAudio
import com.feldelysium.app.domain.repository.SettingsRepository
import com.feldelysium.app.domain.scene.SceneSource
import com.feldelysium.app.domain.sound.SoundCue
import com.feldelysium.app.domain.sound.SoundPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlaybackViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun tapRevealsTheNextLineAndArmsContinue() = runTest(dispatcher) {
        val speaker = Speaker("npc", "Kim", 0xFF010203, type = SpeakerType.NPC)
        val entries = listOf(
            DialogueEntry(id = "1", speaker = speaker, text = "First"),
            DialogueEntry(id = "2", speaker = speaker, text = "Second"),
        )
        val vm = PlaybackViewModel(
            scene = FakeScene(entries),
            audio = PlaybackAudio(SilentPlayer),
            settings = FakeSettings(AppSettings(showContinueButton = true)),
        )

        vm.state.test {
            assertEquals(false, awaitItem().isPanelOpen)
            advanceUntilIdle()
            assertTrue(awaitItem().isPanelOpen)

            vm.dispatch(PlaybackEvent.Tap)
            val revealed = awaitItem()
            assertEquals(listOf("1"), revealed.revealedEntries.map { it.id })
            assertTrue(revealed.prompt is Prompt.Pending)

            advanceUntilIdle()
            val prompted = awaitItem()
            assertTrue(prompted.prompt is Prompt.Continue)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun autoPlayOnAnEmptySceneCompletesImmediately() = runTest(dispatcher) {
        val vm = PlaybackViewModel(
            scene = FakeScene(emptyList()),
            audio = PlaybackAudio(SilentPlayer),
            settings = FakeSettings(AppSettings()),
        )

        vm.autoPlayDone.test {
            assertEquals(false, awaitItem())
            vm.startAutoPlay(1f)
            assertEquals(true, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}

private class FakeScene(entries: List<DialogueEntry>) : SceneSource {
    override val scene: StateFlow<DialogueScene> = MutableStateFlow(DialogueScene(entries = entries))
}

private class FakeSettings(initial: AppSettings) : SettingsRepository {
    override val settings = MutableStateFlow(initial)
    override suspend fun update(transform: (AppSettings) -> AppSettings) {
        settings.value = transform(settings.value)
    }
}

private object SilentPlayer : SoundPlayer {
    override fun play(cue: SoundCue, seed: Int, volume: Float) = Unit
    override fun startLoop(cue: SoundCue, volume: Float) = Unit
    override fun stopLoop(fadeOutMs: Long) = Unit
    override fun preload(cues: Collection<SoundCue>) = Unit
    override fun release() = Unit
}
