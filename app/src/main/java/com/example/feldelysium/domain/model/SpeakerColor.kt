package com.example.feldelysium.domain.model

import kotlin.math.sqrt

enum class SpeakerColor(val argb: Long) {
    INTELLECT(0xFF5BB9C3),
    PSYCHE(0xFF6A43BD),
    PHYSIQUE(0xFFB02E5E),
    MOTORICS(0xFFCAB139),
    PLAIN(0xFFFCFCFC),
    ORANGE(0xFFD9692E),
    BLUE(0xFF4F83D1),
    GREEN(0xFF6CB45B),
    PINK(0xFFD06A8B),
    SAND(0xFF9E7E5E),
    TEAL(0xFF3FA89B),
    GREY(0xFF8C8E8A),
    ;

    companion object {
        val DEFAULT = GREY
        val AllArgb: List<Long> = entries.map { it.argb }

        fun isCustom(argb: Long) = entries.none { it.argb == normalize(argb) }

        fun nearest(argb: Long, maxDistance: Int = 36): SpeakerColor? {
            val c = normalize(argb)
            fun d2(o: Long): Long {
                val dr = ((c shr 16) and 0xFF) - ((o shr 16) and 0xFF)
                val dg = ((c shr 8) and 0xFF) - ((o shr 8) and 0xFF)
                val db = (c and 0xFF) - (o and 0xFF)
                return dr * dr + dg * dg + db * db
            }

            val best = entries.minBy { d2(it.argb) }
            return best.takeIf { sqrt(d2(best.argb).toDouble()) <= maxDistance }
        }

        fun snap(argb: Long): Long = nearest(argb)?.argb ?: normalize(argb)

        fun normalize(argb: Long) = (argb and 0xFFFFFFL) or 0xFF000000L
    }
}
