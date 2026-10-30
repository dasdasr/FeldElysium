package com.feldelysium.app.di

import android.app.Application
import android.content.ContentResolver
import com.feldelysium.app.data.scene.FileSceneRepository
import com.feldelysium.app.data.settings.DataStoreSettingsRepository
import com.feldelysium.app.data.sound.SoundManager
import com.feldelysium.app.data.speaker.SpeakerStorage
import com.feldelysium.app.domain.playback.PlaybackAudio
import com.feldelysium.app.domain.repository.SceneRepository
import com.feldelysium.app.domain.repository.SettingsRepository
import com.feldelysium.app.domain.repository.SpeakerRepository
import com.feldelysium.app.domain.scene.SceneSession
import com.feldelysium.app.domain.scene.SceneSpeakerReconciler
import com.feldelysium.app.domain.sound.SoundPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppContainer(app: Application) {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val speakerRepository: SpeakerRepository = SpeakerStorage.repository(app)
    val settingsRepository: SettingsRepository = DataStoreSettingsRepository(app)
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
