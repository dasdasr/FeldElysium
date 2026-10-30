package com.feldelysium.designsystem.foundation.decal

import kotlin.random.Random

internal object DecalText {
    private val initials = listOf("J.M.", "A.K.", "R.V.", "S.L.", "D.H.")
    private val status = listOf("OK", "OK", "OK", "REVIEW", "HOLD")

    private fun pad(n: Int, digits: Int) = n.toString().padStart(digits, '0')

    fun text(rng: Random, c: DecalContext): String {
        val pool = buildList {
            add("FELD • PLAYBACK • DEVICE")
            add("STATE ${10 + rng.nextInt(90)}")
            add("DEVICE TAPE ${1 + rng.nextInt(4)}")
            add("REC. ${pad(rng.nextInt(60), 4)}-${'A' + rng.nextInt(4)}")
            add("END")
            if (c.sceneName.isNotBlank()) add("${c.sceneName.uppercase().take(18)} • ${c.date}")
            if (c.segmentFrom != null && c.segmentTo != null) {
                add("SEG ${pad(c.segmentFrom, 4)}-${pad(c.segmentTo, 4)}")
            }
            c.speakerName?.let { add("VOICE: ${it.uppercase().take(14)}") }
            c.appVersion?.let { add("FELD V$it") }
        }
        return pool[rng.nextInt(pool.size)]
    }

    fun dataBlock(rng: Random, c: DecalContext): String {
        val from = c.segmentFrom ?: rng.nextInt(1, 40)
        val to = c.segmentTo ?: (from + rng.nextInt(2, 30))
        val side = if (rng.nextBoolean()) 'A' else 'B'
        val lines = listOf(
            "REEL NO. ${1 + rng.nextInt(6)}/SIDE $side/SEGMENT ${pad(from, 4)}-${pad(to, 4)}",
            "TECHNICAL REVIEW: ${status.random(rng)} - ${initials.random(rng)} ${c.date}",
            "AUDIO LEVEL: -${6 + rng.nextInt(12)}dB",
        )
        return lines.take(2 + rng.nextInt(2)).joinToString("\n")
    }

    fun counter(rng: Random, c: DecalContext): String = when (rng.nextInt(4)) {
        0 -> rng.nextInt(10, 99).toString()

        1 -> c.lineNumber?.let { n ->
            "L.${pad(n, 2)}" + (c.lineCount?.let { "/${pad(it, 2)}" } ?: "")
        } ?: "№ ${pad(rng.nextInt(99), 4)}"

        2 -> "REC. ${pad(rng.nextInt(60), 4)}-B"

        else -> "${pad(rng.nextInt(99), 2)}:${pad(rng.nextInt(60), 2)}"
    }

    fun strip(rng: Random, c: DecalContext): String = when (rng.nextInt(3)) {
        0 -> "FELD • PLAYBACK • DEVICE"

        1 -> "REEL ${1 + rng.nextInt(6)} • ${c.date}"

        else -> {
            val s = rng.nextInt(10, 90)
            (0..3).joinToString("    ") { (s + it).toString() }
        }
    }
}
