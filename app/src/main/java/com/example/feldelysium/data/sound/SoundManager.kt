package com.example.feldelysium.data.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

/**
 * Thin wrapper around [SoundPool] for short one-shot sound effects.
 *
 * Lifecycle:
 *   - Constructed in PlaybackViewModel.init
 *   - Released in PlaybackViewModel.onCleared()
 *
 * Loading:
 *   - All [SoundGroup] assets are pre-loaded at construction time.
 *   - play() is a no-op for sounds that haven't finished loading yet,
 *     preventing crashes on rapid early taps.
 */
class SoundManager(context: Context) {

    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(MAX_STREAMS)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    // soundId → SoundGroup for reverse lookup in onLoadComplete
    private val idToGroup  = mutableMapOf<Int, SoundGroup>()
    private val groupToId  = mutableMapOf<SoundGroup, Int>()
    private val readyGroups = mutableSetOf<SoundGroup>()

    init {
        pool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                idToGroup[sampleId]?.let { readyGroups += it }
            }
        }
        SoundGroup.entries.forEach { group ->
            runCatching {
                context.assets.openFd(group.assetPath).use { afd ->
                    val id = pool.load(afd, PRIORITY)
                    idToGroup[id]    = group
                    groupToId[group] = id
                }
            }
        }
    }

    /**
     * Plays the sound for [group] at [volume] (0.0–1.0).
     * Silent no-op if the sound hasn't loaded yet or the asset is missing.
     */
    fun play(group: SoundGroup, volume: Float = 1f) {
        if (group !in readyGroups) return
        groupToId[group]?.let { id ->
            pool.play(id, volume, volume, PRIORITY, 0, PLAYBACK_RATE)
        }
    }

    fun release() = pool.release()

    companion object {
        private const val MAX_STREAMS    = 3
        private const val PRIORITY       = 1
        private const val PLAYBACK_RATE  = 1f
    }
}