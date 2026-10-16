package com.example.feldelysium.domain.sound

interface SoundPlayer {
    fun play(cue: SoundCue, seed: Int = 0, volume: Float = 1f)

    fun startLoop(cue: SoundCue, volume: Float = 1f)
    fun stopLoop(fadeOutMs: Long = 0L)

    fun preload(cues: Collection<SoundCue>)
    fun release()
}
