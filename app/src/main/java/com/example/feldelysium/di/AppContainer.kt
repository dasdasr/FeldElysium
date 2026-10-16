package com.example.feldelysium.di

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.feldelysium.data.scene.FileSceneRepository
import com.example.feldelysium.data.sound.SoundManager
import com.example.feldelysium.data.speaker.SpeakerStorage
import com.example.feldelysium.domain.playback.PlaybackAudio
import com.example.feldelysium.domain.repository.SceneRepository
import com.example.feldelysium.domain.repository.SpeakerRepository
import com.example.feldelysium.domain.sound.SoundPlayer
import com.example.feldelysium.domain.usecase.DeleteSceneUseCase
import com.example.feldelysium.domain.usecase.ExportSceneUseCase
import com.example.feldelysium.domain.usecase.ImportSceneUseCase
import com.example.feldelysium.domain.usecase.ListScenesUseCase
import com.example.feldelysium.domain.usecase.LoadCurrentSceneUseCase
import com.example.feldelysium.domain.usecase.OpenSceneUseCase
import com.example.feldelysium.domain.usecase.SaveSceneUseCase
import com.example.feldelysium.domain.usecase.SceneSpeakerReconciler
import com.example.feldelysium.domain.usecase.SceneUseCases
import com.example.feldelysium.presentation.shared.SceneViewModel

class AppContainer(app: Application) {
    private val speakerRepository: SpeakerRepository = SpeakerStorage.repository(app)
    private val sceneRepository: SceneRepository = FileSceneRepository(app)
    private val reconciler = SceneSpeakerReconciler(speakerRepository)

    val sceneUseCases = SceneUseCases(
        loadCurrent = LoadCurrentSceneUseCase(sceneRepository, reconciler),
        open = OpenSceneUseCase(sceneRepository, reconciler),
        save = SaveSceneUseCase(sceneRepository),
        list = ListScenesUseCase(sceneRepository),
        delete = DeleteSceneUseCase(sceneRepository),
        export = ExportSceneUseCase(sceneRepository),
        import = ImportSceneUseCase(sceneRepository, reconciler),
    )

    val soundPlayer: SoundPlayer by lazy { SoundManager(app) }
    fun playbackAudio() = PlaybackAudio(soundPlayer)
}

class AppViewModelFactory(private val container: AppContainer, private val fallback: ViewModelProvider.Factory) :
    ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
        if (modelClass == SceneViewModel::class.java) {
            SceneViewModel(container.sceneUseCases) as T
        } else {
            fallback.create(modelClass, extras)
        }
}
