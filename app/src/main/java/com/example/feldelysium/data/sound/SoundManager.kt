package com.example.feldelysium.data.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import java.io.File

class SoundManager(context: Context) {

    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(MAX_STREAMS)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    private val idToGroup = mutableMapOf<Int, SoundGroup>()
    private val groupToId = mutableMapOf<SoundGroup, Int>()
    private val readyGroups = mutableSetOf<SoundGroup>()

    private var pendingGroup: SoundGroup? = null
    private var pendingVolume = 1f

    init {
        pool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                idToGroup[sampleId]?.let { readyGroups += it }
            }
            val pending = pendingGroup
            if (pending != null && pending in readyGroups) {
                pendingGroup = null
                start(pending, pendingVolume)
            }
        }
        SoundGroup.entries.forEach { group ->
            runCatching {
                val file = File(context.cacheDir, "sfx_${group.key}.wav")
                context.assets.open(group.assetPath).use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                }
                val id = pool.load(file.absolutePath, PRIORITY)
                if (id != 0) {
                    idToGroup[id] = group
                    groupToId[group] = id
                }
            }
        }
    }

    fun play(group: SoundGroup, volume: Float = 1f) {
        if (group in readyGroups) {
            pendingGroup = null
            start(group, volume)
        } else {
            pendingGroup = group
            pendingVolume = volume
        }
    }

    fun release() = pool.release()

    private fun start(group: SoundGroup, volume: Float) {
        val id = groupToId[group] ?: return
        pool.play(id, volume, volume, PRIORITY, 0, PLAYBACK_RATE)
    }

    companion object {
        private const val MAX_STREAMS = 3
        private const val PRIORITY = 1
        private const val PLAYBACK_RATE = 1f
    }
}
