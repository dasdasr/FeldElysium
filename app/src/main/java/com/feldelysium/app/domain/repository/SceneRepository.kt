package com.feldelysium.app.domain.repository

import com.feldelysium.app.domain.model.DialogueScene
import com.feldelysium.app.domain.model.SceneInfo
import java.io.InputStream
import java.io.OutputStream

interface SceneRepository {
    suspend fun loadCurrent(): DialogueScene?
    suspend fun open(id: String): DialogueScene?
    suspend fun save(scene: DialogueScene)
    suspend fun delete(id: String)
    suspend fun list(): List<SceneInfo>
    suspend fun export(scene: DialogueScene, output: OutputStream)

    suspend fun import(input: InputStream): DialogueScene
}
