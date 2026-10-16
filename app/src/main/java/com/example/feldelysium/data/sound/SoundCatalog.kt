package com.example.feldelysium.data.sound

import com.example.feldelysium.domain.sound.SoundCue
import com.example.feldelysium.domain.sound.SoundCue.*

internal object SoundCatalog {

    private fun sfx(vararg names: String) = names.map { "$it.wav" }

    fun assets(cue: SoundCue): List<String> = when (cue) {
        DialogPanelLoop -> sfx("interface-dialogpanel-loop-01-03")
        DialogPanelOut -> sfx("interface-dialogpanel-out-02-03")
        DialogueClick -> sfx("dialogue-click")
        DiceRoll -> sfx("interface-diceroll-03-01")
        CheckSuccess -> sfx("interface-diceroll-success-02-01")
        CheckFail -> sfx("interface-diceroll-fail-02-01")
        PassiveFys -> sfx("interface-skill-passiveFYS-03-01")
        PassiveInt -> sfx("interface-skill-passiveINT-04-01")
        PassiveMot -> sfx("interface-skill-passiveMOT-04-01")
        PassivePsy -> sfx("interface-skill-passivePSY-04-02")
        HealthDamaged -> sfx("health-damaged-03")
        MoraleDamaged -> sfx("morale-damaged-03")

        SpeakerIntellect -> listOf("intellect_appear.wav")
        SpeakerPsyche -> listOf("psyche_appear.wav")
        SpeakerPhysique -> listOf("physique_appear.wav")
        SpeakerMotorics -> listOf("motorics_appear.wav")
        SpeakerNpc -> listOf("npc_appear.wav")
        SpeakerDefault -> listOf("default_appear.wav")

        UiSelect -> sfx("interface-charcreation-selection-01-01")
        UiClick -> sfx("small-button-click-01", "clicksxf2")
        UiSwitch -> sfx("switch-01", "switch-02", "switch-03", "switch-04")
        UiCancel -> sfx("cancel-01")
        UiSave -> sfx("orb_open_02_01")
        UiDone -> sfx("task-complete-01")
        UiDelete -> sfx("fabric-shuffle-sharp-fail")
        UiTab -> sfx("problem-solution-tabs-01", "tutorial-tab-01")
        UiExport -> sfx("quicksave-complete-01")
    }

    fun pick(cue: SoundCue, seed: Int): String {
        val variants = assets(cue)
        return variants[Math.floorMod(seed, variants.size)]
    }
}
