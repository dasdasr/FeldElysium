package com.feldelysium.app.data.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import com.feldelysium.app.domain.sound.SoundCue
import com.feldelysium.app.domain.sound.SoundPlayer

class SoundManager(context: Context) : SoundPlayer {

    private class Sample {
        var id = 0
        var ready = false
        var failed = false
        var pending: Pending? = null
    }

    private class Pending(val volume: Float, val loopToken: Int?, val at: Long)

    private val appContext = context.applicationContext
    private val main = Handler(Looper.getMainLooper())
    private val samples = HashMap<String, Sample>()
    private val byId = HashMap<Int, Sample>()
    private var pool: SoundPool? = null

    private var loopToken = 0
    private var loopStream = 0
    private var loopPath: String? = null
    private var loopVolume = 1f

    override fun play(cue: SoundCue, seed: Int, volume: Float) {
        val s = sample(SoundCatalog.pick(cue, seed))
        when {
            s.failed -> Unit
            s.ready -> startOnce(s, volume)
            else -> s.pending = Pending(volume, null, SystemClock.uptimeMillis())
        }
    }

    override fun startLoop(cue: SoundCue, volume: Float) {
        val path = SoundCatalog.pick(cue, 0)
        if (loopStream != 0 && loopPath == path) return

        stopLoop()
        val token = loopToken
        loopPath = path
        loopVolume = volume
        val s = sample(path)
        when {
            s.failed -> Unit
            s.ready -> beginLoop(s, token)
            else -> s.pending = Pending(volume, token, SystemClock.uptimeMillis())
        }
    }

    override fun stopLoop(fadeOutMs: Long) {
        loopToken++
        val stream = loopStream
        val volume = loopVolume
        loopStream = 0
        loopPath = null
        val p = pool
        if (stream == 0 || p == null) return
        if (fadeOutMs <= 0L) {
            p.stop(stream)
            return
        }
        val startedAt = SystemClock.uptimeMillis()
        main.post(object : Runnable {
            override fun run() {
                val current = pool ?: return
                val t = ((SystemClock.uptimeMillis() - startedAt) / fadeOutMs.toFloat()).coerceIn(0f, 1f)
                val v = volume * (1f - t)
                current.setVolume(stream, v, v)
                if (t >= 1f) current.stop(stream) else main.postDelayed(this, FADE_STEP_MS)
            }
        })
    }

    override fun preload(cues: Collection<SoundCue>) {
        cues.forEach { cue -> SoundCatalog.assets(cue).forEach { sample(it) } }
    }

    override fun release() {
        main.removeCallbacksAndMessages(null)
        pool?.release()
        pool = null
        samples.clear()
        byId.clear()
        loopToken++
        loopStream = 0
        loopPath = null
    }

    private fun pool(): SoundPool = pool ?: SoundPool.Builder()
        .setMaxStreams(MAX_STREAMS)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()
        .also {
            it.setOnLoadCompleteListener { _, sampleId, status -> onLoaded(sampleId, status) }
            pool = it
        }

    private fun sample(path: String): Sample {
        samples[path]?.let { return it }
        val s = Sample()
        samples[path] = s
        val id = runCatching {
            appContext.assets.openFd(path).use { pool().load(it, PRIORITY) }
        }.onFailure { Log.w("SoundManager", "can't load $path", it) }.getOrDefault(0)
        if (id == 0) {
            s.failed = true
        } else {
            s.id = id
            byId[id] = s
        }
        return s
    }

    private fun onLoaded(sampleId: Int, status: Int) {
        val s = byId[sampleId] ?: return

        if (status != 0) {
            s.failed = true
            s.pending = null
            return
        }

        s.ready = true

        val p = s.pending
        if (p != null) {
            s.pending = null

            val isFresh = SystemClock.uptimeMillis() - p.at <= STALE_MS
            if (isFresh) {
                if (p.loopToken != null) {
                    beginLoop(s, p.loopToken)
                } else {
                    startOnce(s, p.volume)
                }
            }
        }
    }

    private fun startOnce(s: Sample, volume: Float) {
        pool?.play(s.id, volume, volume, PRIORITY, 0, RATE)
    }

    private fun beginLoop(s: Sample, token: Int) {
        if (token != loopToken) return
        loopStream = pool?.play(s.id, loopVolume, loopVolume, PRIORITY, -1, RATE) ?: 0
    }

    private companion object {
        const val MAX_STREAMS = 6
        const val PRIORITY = 1
        const val RATE = 1f
        const val STALE_MS = 400L
        const val FADE_STEP_MS = 20L
    }
}
