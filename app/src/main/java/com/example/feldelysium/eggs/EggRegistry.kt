package com.example.feldelysium.eggs

import com.example.feldelysium.domain.registry.PredefinedSpeakers
import com.example.feldelysium.domain.registry.PredefinedSpeakers.AUTHORITY
import com.example.feldelysium.domain.registry.PredefinedSpeakers.DRAMA
import com.example.feldelysium.domain.registry.PredefinedSpeakers.ELECTROCHEMISTRY
import com.example.feldelysium.domain.registry.PredefinedSpeakers.EMPATHY
import com.example.feldelysium.domain.registry.PredefinedSpeakers.ENDURANCE
import com.example.feldelysium.domain.registry.PredefinedSpeakers.HALF_LIGHT
import com.example.feldelysium.domain.registry.PredefinedSpeakers.HAND_EYE_COORD
import com.example.feldelysium.domain.registry.PredefinedSpeakers.INLAND_EMPIRE
import com.example.feldelysium.domain.registry.PredefinedSpeakers.LOGIC
import com.example.feldelysium.domain.registry.PredefinedSpeakers.RHETORIC
import com.example.feldelysium.domain.registry.PredefinedSpeakers.SUGGESTION
import com.example.feldelysium.domain.registry.PredefinedSpeakers.VISUAL_CALCULUS
import com.example.feldelysium.domain.registry.PredefinedSpeakers.VOLITION
import com.example.feldelysium.eggs.EggRarity.Frequent
import kotlin.time.Duration.Companion.seconds

object EggRegistry {

    private val diamondReplies = listOf(
        skillCheck(PredefinedSpeakers.COMPOSURE, "You are, technically, holding a phone.", Difficulty.Easy),
        skillCheck(
            PredefinedSpeakers.PERCEPTION,
            "Seven taps. The diamond didn't blink. Neither did you.",
            Difficulty.Medium,
        ),
        skillCheck(LOGIC, "A decorative diamond. Seven taps of evidence.", Difficulty.Easy),
        say(ELECTROCHEMISTRY, "Tap it again. Tap it for me."),
        skillCheck(
            INLAND_EMPIRE,
            "The diamond hums back. Faintly. You pretend not to hear it.",
            Difficulty.Challenging,
        ),
        skillCheck(
            VOLITION,
            "You could stop. You won't.",
            Difficulty.Medium,
            CheckResult.Failure,
        ),
        skillCheck(HALF_LIGHT, "It's watching you. Probably.", Difficulty.Heroic),
        skillCheck(
            PredefinedSpeakers.INTERFACING,
            "Settings. Where machines confess what they can do.",
            Difficulty.Medium,
            scope = EggScreen.Settings,
        ),
        skillCheck(
            AUTHORITY,
            "You are tuning the tool. Good. Own it.",
            Difficulty.Easy,
            scope = EggScreen.Settings,
        ),
        skillCheck(
            RHETORIC,
            "Seven taps. A dramatic pause before your next line.",
            Difficulty.Medium,
            scope = EggScreen.SceneEditor,
        ),
        skillCheck(
            DRAMA,
            "Nobody is watching. Perform anyway.",
            Difficulty.Challenging,
            scope = EggScreen.SceneEditor,
        ),
        skillCheck(
            EMPATHY,
            "They are all waiting. Every one of them wants to speak.",
            Difficulty.Easy,
            scope = EggScreen.ChooseSpeaker,
        ),
        skillCheck(
            SUGGESTION,
            "Pick the loudest one. Or the quietest. They'll both talk.",
            Difficulty.Medium,
            scope = EggScreen.ChooseSpeaker,
        ),
    )

    private val switchReplies = listOf(
        say(ELECTROCHEMISTRY, "Again. Again."),
        say(ELECTROCHEMISTRY, "Click. Click. Click. More."),
        skillCheck(HAND_EYE_COORD, "Your thumb has found its rhythm.", Difficulty.Easy),
        skillCheck(
            VOLITION,
            "It's a switch. It has two states. You've seen both.",
            Difficulty.Medium,
            CheckResult.Failure,
        ),
    )

    private val nothingToResetReplies = listOf(
        say(VOLITION, "Nothing to reset. You are who you are."),
        say(LOGIC, "Resetting the unchanged. A closed loop."),
        skillCheck(AUTHORITY, "No change detected. Carry on.", Difficulty.Trivial),
    )

    private val fabReplies = listOf(
        skillCheck(
            AUTHORITY,
            "You couldn't decide. So I decided for you.",
            result = CheckResult.Success,
        ),
        skillCheck(SUGGESTION, "Don't think. Let the chips fall.", Difficulty.Easy),
        skillCheck(
            INLAND_EMPIRE,
            "The speaker chose you. Not the other way around.",
            Difficulty.Challenging,
        ),
    )

    private val soonReplies = listOf(
        skillCheck(VOLITION, "Patience. It's called 'Soon', not 'Now'.", Difficulty.Medium),
        say(ELECTROCHEMISTRY, "Now. NOW. Why isn't it NOW?"),
        say(AUTHORITY, "You can't tap a feature into existence."),
        say(LOGIC, "Soon: adverb. Meaning: not yet. Hence the label."),
        skillCheck(ENDURANCE, "You can wait. You have waited for worse.", Difficulty.Easy),
        say(
            ELECTROCHEMISTRY,
            "Autoplay. As if you'd ever stop pressing things yourself.",
            scope = SoonFeature.Autoplay,
        ),
        say(
            LOGIC,
            "A delay between lines. Time is just a number you can set later.",
            scope = SoonFeature.LineDelay,
        ),
        skillCheck(
            VISUAL_CALCULUS,
            "Icons that you will tap. Eventually. Calculated.",
            Difficulty.Medium,
            scope = SoonFeature.StatusIcons,
        ),
    )

    val all: List<Egg> = listOf(
        Egg(
            id = "header.diamond.7",
            rarity = Frequent,
            replies = diamondReplies,
            trigger = burst(EggEvent.HeaderDiamond, count = 7, within = 3.seconds),
        ),
        Egg(
            id = "switch.spam.7",
            rarity = Frequent,
            replies = switchReplies,
            trigger = burst(EggEvent.SwitchToggle, count = 7, within = 5.seconds),
        ),
        Egg(
            id = "reset.nothing",
            rarity = Frequent,
            replies = nothingToResetReplies,
            trigger = onEvent(EggEvent.ResetUnchanged),
        ),
        Egg(
            id = "fab.longpress",
            rarity = Frequent,
            replies = fabReplies,
            trigger = onEvent(EggEvent.FabLongPress),
        ),
        Egg(
            id = "soon.tap",
            rarity = Frequent,
            replies = soonReplies,
            cooldown = 1.seconds, // 20
            trigger = onEvent(EggEvent.SoonTap),
        ),

        // ── Eggzamples for future formats (uncomment and replace with actual events) ──
        //
        // Action pattern: opened Editor → added → immediately deleted → added again
        // Egg("pattern.add.delete.add", Rare,
        //     replies = listOf(say(PredefinedSpeakers.LOGIC, "A perfectly rational sequence of events.")),
        //     trigger = inOrder("entry.add", "entry.delete", "entry.add", within = 15.seconds))
        //
        // "Anomalous" behavior within a limited time window
        // Egg("anomaly.cinema", Frequent,
        //     replies = listOf(say(PredefinedSpeakers.ELECTROCHEMISTRY, "Cinema. No cinema. Cinema. No cinema.")),
        //     trigger = burst("switch.toggle", 4, 8.seconds, value = "Cinema frame"))
        //
        // Time-based: the app is opened late at night (Rare: multiple replies → up to N times per day, without repeats)
        // Egg("time.night", Rare,
        //     replies = listOf(say("Shivers", "The city is drowning in the shroud of sleep.")),
        //     trigger = timeBetween(1, 5))
        //
        // Secret based on a specific value: report("status.reals", "130") and report("status.day", "1")
        // Egg("secret.130", Secret,
        //     replies = listOf(skillCheck(PredefinedSpeakers.LOGIC, "130 réal. The exact amount of your outstanding debt. Fascinating.")),
        //     trigger = onEvent("status.reals", "130"))
        //
        // UltraRare: any number with a small chance
        // Egg("ultra.number", UltraRare,
        //     replies = listOf(say(PredefinedSpeakers.LOGIC, "This number means nothing. Obviously.")),
        //     trigger = whenValue("status.reals") { it.toIntOrNull() != null }, chance = 0.03f)
    )
}
