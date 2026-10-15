package com.example.feldelysium.di

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.feldelysium.data.scene.FileSceneRepository
import com.example.feldelysium.data.speaker.SpeakerStorage
import com.example.feldelysium.domain.repository.SceneRepository
import com.example.feldelysium.domain.repository.SpeakerRepository
import com.example.feldelysium.domain.usecase.*
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
}

class AppViewModelFactory(
    private val container: AppContainer,
    private val fallback: ViewModelProvider.Factory,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
        if (modelClass == SceneViewModel::class.java) {
            SceneViewModel(container.sceneUseCases) as T
        } else {
            fallback.create(modelClass, extras)
        }
}
