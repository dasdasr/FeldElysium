package com.feldelysium.app.data.sound

import com.feldelysium.app.domain.sound.SoundCue

internal object SoundCatalog {

    private fun sfx(vararg names: String) = names.map { "$it.wav" }

    fun assets(cue: SoundCue): List<String> = interfaceAssets(cue) ?: characterAssets(cue)

    private fun interfaceAssets(cue: SoundCue): List<String>? = commonInterfaceAssets(cue) ?: actionInterfaceAssets(cue)

    private fun commonInterfaceAssets(cue: SoundCue): List<String>? = when (cue) {
        SoundCue.DialogPanelLoop -> sfx("interface-dialogpanel-loop-01-03")
        SoundCue.DialogPanelOut -> sfx("interface-dialogpanel-out-02-03")
        SoundCue.DialogueClick -> sfx("dialogue-click")
        SoundCue.DiceRoll -> sfx("interface-diceroll-03-01")
        SoundCue.CheckSuccess -> sfx("interface-diceroll-success-02-01")
        SoundCue.CheckFail -> sfx("interface-diceroll-fail-02-01")
        SoundCue.PassiveFys -> sfx("interface-skill-passiveFYS-03-01")
        SoundCue.PassiveInt -> sfx("interface-skill-passiveINT-04-01")
        SoundCue.PassiveMot -> sfx("interface-skill-passiveMOT-04-01")
        SoundCue.PassivePsy -> sfx("interface-skill-passivePSY-04-02")
        SoundCue.HealthDamaged -> sfx("health-damaged-03")
        SoundCue.MoraleDamaged -> sfx("morale-damaged-03")
        else -> null
    }

    private fun actionInterfaceAssets(cue: SoundCue): List<String>? = when (cue) {
        SoundCue.UiSelect -> sfx("interface-charcreation-selection-01-01")
        SoundCue.UiClick -> sfx("small-button-click-01", "clicksxf2")
        SoundCue.UiSwitch -> sfx("switch-01", "switch-02", "switch-03", "switch-04")
        SoundCue.UiCancel -> sfx("cancel-01")
        SoundCue.UiSave -> sfx("orb_open_02_01")
        SoundCue.UiDone -> sfx("task-complete-01")
        SoundCue.UiDelete -> sfx("fabric-shuffle-sharp-fail")
        SoundCue.UiTab -> sfx("problem-solution-tabs-01", "tutorial-tab-01")
        SoundCue.UiExport -> sfx("quicksave-complete-01")
        else -> null
    }

    private fun characterAssets(cue: SoundCue): List<String> = when (cue) {
        SoundCue.SpeakerIntellect -> listOf("intellect_appear.wav")
        SoundCue.SpeakerPsyche -> listOf("psyche_appear.wav")
        SoundCue.SpeakerPhysique -> listOf("physique_appear.wav")
        SoundCue.SpeakerMotorics -> listOf("motorics_appear.wav")
        SoundCue.SpeakerNpc -> listOf("npc_appear.wav")
        SoundCue.SpeakerDefault -> listOf("default_appear.wav")
        else -> error("Missing sound assets for $cue")
    }

    fun pick(cue: SoundCue, seed: Int): String {
        val variants = assets(cue)
        return variants[Math.floorMod(seed, variants.size)]
    }
}
