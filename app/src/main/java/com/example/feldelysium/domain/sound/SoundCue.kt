package com.example.feldelysium.domain.sound

import com.example.feldelysium.domain.model.SoundKeys

enum class SoundCue {
    DialogPanelLoop, DialogPanelOut, DialogueClick,
    DiceRoll, CheckSuccess, CheckFail,
    PassiveFys, PassiveInt, PassiveMot, PassivePsy,
    HealthDamaged, MoraleDamaged,
    SpeakerIntellect, SpeakerPsyche, SpeakerPhysique, SpeakerMotorics, SpeakerNpc, SpeakerDefault,

    UiSelect, UiClick, UiSwitch, UiCancel, UiSave, UiDone, UiDelete, UiTab, UiExport,
    ;

    companion object {
        private val passives = listOf(PassiveFys, PassiveInt, PassiveMot, PassivePsy)

        val playbackSet = listOf(
            DialogPanelLoop, DialogPanelOut, DialogueClick,
            DiceRoll, CheckSuccess, CheckFail, HealthDamaged, MoraleDamaged,
            SpeakerIntellect, SpeakerPsyche, SpeakerPhysique, SpeakerMotorics, SpeakerNpc, SpeakerDefault,
        ) + passives

        val uiSet = listOf(UiSelect, UiClick, UiSwitch, UiCancel, UiSave, UiDone, UiDelete, UiTab, UiExport)

        fun forSpeakerKey(key: String): SoundCue? = when (key) {
            SoundKeys.NONE -> null
            SoundKeys.INTELLECT -> SpeakerIntellect
            SoundKeys.PSYCHE -> SpeakerPsyche
            SoundKeys.PHYSIQUE -> SpeakerPhysique
            SoundKeys.MOTORICS -> SpeakerMotorics
            SoundKeys.NPC -> SpeakerNpc
            else -> SpeakerDefault
        }

        fun passiveFor(key: String, seed: Int): SoundCue = when (key) {
            SoundKeys.PHYSIQUE -> PassiveFys
            SoundKeys.INTELLECT -> PassiveInt
            SoundKeys.MOTORICS -> PassiveMot
            SoundKeys.PSYCHE -> PassivePsy
            else -> passives[Math.floorMod(seed, passives.size)]
        }
    }
}
