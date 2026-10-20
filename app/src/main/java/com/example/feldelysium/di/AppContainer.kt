package com.example.feldelysium.di

import android.app.Application
import android.content.ContentResolver
import com.example.feldelysium.data.scene.FileSceneRepository
import com.example.feldelysium.data.settings.SettingsRepository
import com.example.feldelysium.data.sound.SoundManager
import com.example.feldelysium.data.speaker.SpeakerStorage
import com.example.feldelysium.domain.SceneSpeakerReconciler
import com.example.feldelysium.domain.playback.PlaybackAudio
import com.example.feldelysium.domain.repository.SceneRepository
import com.example.feldelysium.domain.repository.SpeakerRepository
import com.example.feldelysium.domain.sound.SoundPlayer
import com.example.feldelysium.presentation.shared.SceneSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppContainer(app: Application) {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val speakerRepository: SpeakerRepository = SpeakerStorage.repository(app)
    val settingsRepository = SettingsRepository(app)
    val contentResolver: ContentResolver = app.contentResolver
    val soundPlayer: SoundPlayer by lazy { SoundManager(app) }

    private val sceneRepository: SceneRepository = FileSceneRepository(app)

    val sceneSession = SceneSession(
        repository = sceneRepository,
        speakers = speakerRepository,
        reconciler = SceneSpeakerReconciler(speakerRepository),
        scope = appScope,
    ).also { it.start() }

    fun playbackAudio() = PlaybackAudio(soundPlayer)
}
