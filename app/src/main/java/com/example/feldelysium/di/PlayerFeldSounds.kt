package com.example.feldelysium.di

import com.example.feldelysium.designsystem.foundation.sound.FeldSounds
import com.example.feldelysium.domain.sound.SoundCue
import com.example.feldelysium.domain.sound.SoundPlayer

class PlayerFeldSounds(private val player: SoundPlayer) : FeldSounds {
    override fun click(seed: Int) = player.play(SoundCue.UiClick, seed)
    override fun select() = player.play(SoundCue.UiSelect)
    override fun toggle(seed: Int) = player.play(SoundCue.UiSwitch, seed)
    override fun cancel() = player.play(SoundCue.UiCancel)
    override fun save() = player.play(SoundCue.UiSave)
    override fun delete() = player.play(SoundCue.UiDelete)
    override fun tab(seed: Int) = player.play(SoundCue.UiTab, seed)
}
