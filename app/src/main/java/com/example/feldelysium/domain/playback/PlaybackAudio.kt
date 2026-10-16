package com.example.feldelysium.domain.playback

import com.example.feldelysium.domain.model.CheckResult
import com.example.feldelysium.domain.model.DamageType
import com.example.feldelysium.domain.model.DialogueEntry
import com.example.feldelysium.domain.sound.SoundCue
import com.example.feldelysium.domain.sound.SoundPlayer

class PlaybackAudio(private val player: SoundPlayer) {

    fun preload() = player.preload(SoundCue.playbackSet)

    fun panelOut() {
        player.stopLoop()
        player.play(SoundCue.DialogPanelOut)
    }

    fun click() = player.play(SoundCue.DialogueClick)

    fun diceRoll() = player.play(SoundCue.DiceRoll)

    fun checkResult(result: CheckResult) =
        player.play(if (result == CheckResult.Success) SoundCue.CheckSuccess else SoundCue.CheckFail)

    fun lineRevealed(entry: DialogueEntry) {
        val check = entry.check?.takeIf { entry.speaker.type.allowsChecks }
        val seed = entry.id.hashCode()
        val cue = when {
            check == null -> SoundCue.forSpeakerKey(entry.speaker.soundKey)
            check.active -> null
            else -> SoundCue.passiveFor(entry.speaker.soundKey, seed)
        }
        cue?.let { player.play(it, seed) }
    }

    fun damage(type: DamageType) =
        player.play(if (type == DamageType.Health) SoundCue.HealthDamaged else SoundCue.MoraleDamaged)

    fun startScrollLoop() = player.startLoop(SoundCue.DialogPanelLoop, LOOP_VOLUME)
    fun stopScrollLoop() = player.stopLoop(SCROLL_FADE_MS)

    fun stop() = player.stopLoop()

    private companion object {
        const val LOOP_VOLUME = 0.7f
        const val SCROLL_FADE_MS = 180L
    }
}
