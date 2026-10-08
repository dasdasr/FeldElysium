package com.example.feldelysium.eggs

import com.example.feldelysium.eggs.EggRarity.Frequent
import kotlin.time.Duration.Companion.seconds

object EggRegistry {

    private val diamondReplies = listOf(
        skillCheck("Composure", "You are, technically, holding a phone.", Difficulty.Easy),
        skillCheck(
            "Perception",
            "Seven taps. The diamond didn't blink. Neither did you.",
            Difficulty.Medium
        ),
        skillCheck("Logic", "A decorative diamond. Seven taps of evidence.", Difficulty.Easy),
        say("Electrochemistry", "Tap it again. Tap it for me."),
        skillCheck(
            "Inland Empire",
            "The diamond hums back. Faintly. You pretend not to hear it.",
            Difficulty.Challenging
        ),
        skillCheck(
            "Volition",
            "You could stop. You won't.",
            Difficulty.Medium,
            CheckResult.Failure
        ),
        skillCheck("Half Light", "It's watching you. Probably.", Difficulty.Heroic),
        skillCheck(
            "Interfacing",
            "Settings. Where machines confess what they can do.",
            Difficulty.Medium,
            scope = "Settings"
        ),
        skillCheck(
            "Authority",
            "You are tuning the tool. Good. Own it.",
            Difficulty.Easy,
            scope = "Settings"
        ),
        skillCheck(
            "Rhetoric",
            "Seven taps. A dramatic pause before your next line.",
            Difficulty.Medium,
            scope = "Scene editor"
        ),
        skillCheck(
            "Drama",
            "Nobody is watching. Perform anyway.",
            Difficulty.Challenging,
            scope = "Scene editor"
        ),
        skillCheck(
            "Empathy",
            "They are all waiting. Every one of them wants to speak.",
            Difficulty.Easy,
            scope = "Choose speaker"
        ),
        skillCheck(
            "Suggestion",
            "Pick the loudest one. Or the quietest. They'll both talk.",
            Difficulty.Medium,
            scope = "Choose speaker"
        ),
    )

    private val switchReplies = listOf(
        say("Electrochemistry", "Again. Again."),
        say("Electrochemistry", "Click. Click. Click. More."),
        skillCheck("Hand/Eye Coordination", "Your thumb has found its rhythm.", Difficulty.Easy),
        skillCheck(
            "Volition",
            "It's a switch. It has two states. You've seen both.",
            Difficulty.Medium,
            CheckResult.Failure
        ),
    )

    private val nothingToResetReplies = listOf(
        say("Volition", "Nothing to reset. You are who you are."),
        say("Logic", "Resetting the unchanged. A closed loop."),
        skillCheck("Authority", "No change detected. Carry on.", Difficulty.Trivial),
    )

    private val fabReplies = listOf(
        skillCheck(
            "Authority",
            "You couldn't decide. So I decided for you.",
            result = CheckResult.Success
        ),
        skillCheck("Suggestion", "Don't think. Let the chips fall.", Difficulty.Easy),
        skillCheck(
            "Inland Empire",
            "The speaker chose you. Not the other way around.",
            Difficulty.Challenging
        ),
    )

    private val soonReplies = listOf(
        skillCheck("Volition", "Patience. It's called 'Soon', not 'Now'.", Difficulty.Medium),
        say("Electrochemistry", "Now. NOW. Why isn't it NOW?"),
        say("Authority", "You can't tap a feature into existence."),
        say("Logic", "Soon: adverb. Meaning: not yet. Hence the label."),
        skillCheck("Endurance", "You can wait. You have waited for worse.", Difficulty.Easy),
        say(
            "Electrochemistry",
            "Autoplay. As if you'd ever stop pressing things yourself.",
            scope = "Autoplay"
        ),
        say(
            "Logic",
            "A delay between lines. Time is just a number you can set later.",
            scope = "Line delay"
        ),
        skillCheck(
            "Visual Calculus",
            "Icons that you will tap. Eventually. Calculated.",
            Difficulty.Medium,
            scope = "Status icons"
        ),
    )

    val all: List<Egg> = listOf(
        Egg(
            id = "header.diamond.7",
            rarity = Frequent,
            replies = diamondReplies,
            trigger = burst("header.diamond", count = 7, within = 3.seconds),
        ),
        Egg(
            id = "switch.spam.7",
            rarity = Frequent,
            replies = switchReplies,
            trigger = burst("switch.toggle", count = 7, within = 5.seconds),
        ),
        Egg(
            id = "reset.nothing",
            rarity = Frequent,
            replies = nothingToResetReplies,
            trigger = onEvent("settings.reset", "nochange"),
        ),
        Egg(
            id = "fab.longpress",
            rarity = Frequent,
            replies = fabReplies,
            trigger = onEvent("fab.longpress"),
        ),
        Egg(
            id = "soon.tap",
            rarity = Frequent,
            replies = soonReplies,
            cooldown = 1.seconds, //20
            trigger = onEvent("soon.tap"),
        ),

        // ── Examples for future formats (uncomment and replace with actual events) ──
        //
        // Action pattern: opened Editor → added → immediately deleted → added again
        // Egg("pattern.add.delete.add", Rare,
        //     replies = listOf(say("Logic", "A perfectly rational sequence of events.")),
        //     trigger = inOrder("entry.add", "entry.delete", "entry.add", within = 15.seconds))
        //
        // "Anomalous" behavior within a limited time window
        // Egg("anomaly.cinema", Frequent,
        //     replies = listOf(say("Electrochemistry", "Cinema. No cinema. Cinema. No cinema.")),
        //     trigger = burst("switch.toggle", 4, 8.seconds, value = "Cinema frame"))
        //
        // Time-based: the app is opened late at night (Rare: multiple replies → up to N times per day, without repeats)
        // Egg("time.night", Rare,
        //     replies = listOf(say("Shivers", "The city is drowning in the shroud of sleep.")),
        //     trigger = timeBetween(1, 5))
        //
        // Secret based on a specific value: report("status.reals", "130") and report("status.day", "1")
        // Egg("secret.130", Secret,
        //     replies = listOf(skillCheck("Logic", "130 réal. The exact amount of your outstanding debt. Fascinating.")),
        //     trigger = onEvent("status.reals", "130"))
        //
        // UltraRare: any number with a small chance
        // Egg("ultra.number", UltraRare,
        //     replies = listOf(say("Logic", "This number means nothing. Obviously.")),
        //     trigger = whenValue("status.reals") { it.toIntOrNull() != null }, chance = 0.03f)
    )
}
