package com.feldelysium.app.data.speaker

import android.content.Context
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import com.feldelysium.app.domain.model.SpeakerDocument
import com.feldelysium.app.domain.repository.SpeakerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File

object SpeakerStorage {
    @Volatile
    private var instance: DataStoreSpeakerRepository? = null

    fun repository(context: Context): SpeakerRepository = instance ?: synchronized(this) {
        instance ?: create(context.applicationContext).also { instance = it }
    }

    fun icons(context: Context) = SpeakerIconStore(context)

    private fun create(app: Context): DataStoreSpeakerRepository {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val file = File(app.filesDir, "datastore/speakers.json").apply { parentFile?.mkdirs() }
        val store = DataStoreFactory.create(
            serializer = SpeakerDocumentSerializer,
            corruptionHandler = ReplaceFileCorruptionHandler {
                runCatching { file.copyTo(File(file.path + ".corrupt"), overwrite = true) }
                SpeakerDocument()
            },
            scope = scope,
            produceFile = { file },
        )
        return DataStoreSpeakerRepository(app, SpeakerIconStore(app), store).also { repo ->
            scope.launch { repo.pruneIcons() }
        }
    }
}
