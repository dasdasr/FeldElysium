package com.feldelysium.app.domain.usecase

import com.feldelysium.app.domain.scene.SceneSession
import java.io.InputStream
import java.io.OutputStream

class SceneTransferUseCase(private val session: SceneSession) {
    suspend fun export(output: OutputStream) {
        output.use { session.exportScene(it) }
    }

    suspend fun import(input: InputStream) {
        input.use { session.importScene(it) }
    }
}
