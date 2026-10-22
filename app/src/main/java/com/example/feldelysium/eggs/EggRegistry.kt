package com.example.feldelysium.eggs

import com.example.feldelysium.domain.registry.PredefinedSpeakers.BREATHWARDEN
import com.example.feldelysium.domain.registry.PredefinedSpeakers.EVASION
import com.example.feldelysium.domain.registry.PredefinedSpeakers.GROUNDING
import com.example.feldelysium.domain.registry.PredefinedSpeakers.OFF_KILTER
import com.example.feldelysium.domain.registry.PredefinedSpeakers.TENDERNESS
import com.example.feldelysium.domain.registry.PredefinedSpeakers.THE_UNSAID
import com.example.feldelysium.domain.registry.PredefinedSpeakers.WORLDWRIGHT
import com.example.feldelysium.domain.registry.PredefinedSpeakers.WORTH
import com.example.feldelysium.eggs.EggRarity.Frequent
import kotlin.time.Duration.Companion.seconds

object EggRegistry {

    private val diamondReplies = listOf(
        skillCheck(
            BREATHWARDEN,
            "You're holding your breath. For a diamond. On a phone. Get a grip.",
            Difficulty.Easy,
        ),
        skillCheck(
            OFF_KILTER,
            "Seven taps. The diamond hasn't blinked. You have. Probably.",
            Difficulty.Medium,
        ),
        skillCheck(WORTH, "A decorative diamond. Seven taps of evidence.", Difficulty.Easy),
        say(
            OFF_KILTER,
            "Tap it again. Maybe the eighth time it'll grow a little hat.",
        ),
        skillCheck(
            WORLDWRIGHT,
            "The diamond hums back. Faintly. You pretend not to hear it.",
            Difficulty.Challenging,
        ),
        skillCheck(
            BREATHWARDEN,
            "You could stop. You won't.",
            Difficulty.Medium,
            CheckResult.Failure,
        ),
        skillCheck(OFF_KILTER, "It's watching you. Probably.", Difficulty.Heroic),
        skillCheck(
            WORLDWRIGHT,
            "Settings. Where machines confess what they can do.",
            Difficulty.Medium,
            scope = EggScreen.Settings,
        ),
        skillCheck(
            WORTH,
            "You are tuning the tool. Finally, something you can control without disappointing anyone.",
            Difficulty.Easy,
            scope = EggScreen.Settings,
        ),
        skillCheck(
            THE_UNSAID,
            "Seven taps. A dramatic pause before your next line.",
            Difficulty.Medium,
            scope = EggScreen.SceneEditor,
        ),
        skillCheck(
            THE_UNSAID,
            "Nobody is watching. Which is exactly what someone would say if they were watching.",
            Difficulty.Challenging,
            scope = EggScreen.SceneEditor,
        ),
        skillCheck(
            TENDERNESS,
            "They are all waiting. Every one of them wants to speak.",
            Difficulty.Easy,
            scope = EggScreen.ChooseSpeaker,
        ),
        skillCheck(
            TENDERNESS,
            "Pick the loudest one.",
            Difficulty.Medium,
            scope = EggScreen.ChooseSpeaker,
        ),
    )

    private val switchReplies = listOf(
        say(OFF_KILTER, "Again. Again. The button knows what it did."),
        say(OFF_KILTER, "Click. Click. Click. There. You've intimidated the rectangle."),
        say(OFF_KILTER, "Another click-clack. Which side is it on now? Wait, which side are you on?"),
        skillCheck(EVASION, "Your thumb has found its rhythm.", Difficulty.Easy),
        skillCheck(
            BREATHWARDEN,
            "It's a switch. It has two states. You've seen both.",
            Difficulty.Medium,
            CheckResult.Failure,
        ),
    )

    private val nothingToResetReplies = listOf(
        say(BREATHWARDEN, "Nothing to reset. You are who you are."),
        say(WORTH, "Resetting the unchanged. A closed loop."),
        skillCheck(
            GROUNDING,
            "No change detected. For now.",
            Difficulty.Trivial,
        ),
    )

    private val fabReplies = listOf(
        skillCheck(
            EVASION,
            "You couldn't decide. So...",
            result = CheckResult.Success,
        ),
        skillCheck(OFF_KILTER, "Don't think. Let the chips fall. Falling is a skill.", Difficulty.Easy),
        skillCheck(
            WORLDWRIGHT,
            "The speaker chose you. Not the other way around.",
            Difficulty.Challenging,
        ),
    )

    private val soonReplies = listOf(
        skillCheck(BREATHWARDEN, "Patience. It's called 'Soon', not 'Now'.", Difficulty.Medium),
        say(EVASION, "Now. NOW. Why isn't it NOW? Fine. Leaving. Coming back. Still 'Soon'."),
        say(WORLDWRIGHT, "You can't tap a feature into existence."),
        say(WORTH, "Soon: adverb. Meaning: not yet. Hence the label."),
        skillCheck(GROUNDING, "You can wait. You have waited for worse.", Difficulty.Easy),
        say(
            THE_UNSAID,
            "Autoplay? You trust others to press buttons for you?",
            scope = SoonFeature.Autoplay,
        ),
        say(
            WORTH,
            "A delay between lines. Time is just a number you can set later.",
            scope = SoonFeature.LineDelay,
        ),
        skillCheck(
            WORLDWRIGHT,
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
        //     replies = listOf(say(WORTH, "A perfectly rational sequence of events.")),
        //     trigger = inOrder("entry.add", "entry.delete", "entry.add", within = 15.seconds))
        //
        // "Anomalous" behavior within a limited time window
        // Egg("anomaly.cinema", Frequent,
        //     replies = listOf(say(OFF_KILTER, "Cinema. No cinema. Cinema. No cinema.")),
        //     trigger = burst("switch.toggle", 4, 8.seconds, value = "Cinema frame"))
        //
        // Time-based: the app is opened late at night (Rare: multiple replies → up to N times per day, without repeats)
        // Egg("time.night", Rare,
        //     replies = listOf(say(BREATHWARDEN, "3 a.m. Your body is asking why you're awake.")),
        //     trigger = timeBetween(1, 5))
        //
        // Secret based on a specific value: report("status.reals", "130") and report("status.day", "1")
        // Egg("secret.130", Secret,
        //     replies = listOf(skillCheck(WORTH, "130 réal. The exact amount of your outstanding debt. Fascinating.")),
        //     trigger = onEvent("status.reals", "130"))
        //
        // UltraRare: any number with a small chance
        // Egg("ultra.number", UltraRare,
        //     replies = listOf(say(WORTH, "This number means nothing. Obviously.")),
        //     trigger = whenValue("status.reals") { it.toIntOrNull() != null }, chance = 0.03f)
    )
}
