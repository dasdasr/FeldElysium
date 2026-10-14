package com.example.feldelysium.domain.model

data class Speaker(
    val id: String,
    val name: String,
    val colorArgb: Long,
    val iconResId: Int? = null,
    val customIconUri: String? = null,
    val type: SpeakerType = SpeakerType.CUSTOM,
    val isBundled: Boolean = false,
) {
    val soundKey: String get() = type.soundKey ?: SoundKeys.DEFAULT

    companion object {
        const val DEFAULT_CUSTOM_COLOR: Long = 0xFF4F83D1
    }
}

fun List<Speaker>.groupedByTag(tagOrder: List<SpeakerType>): List<Pair<SpeakerType, List<Speaker>>> {
    val byId = groupBy { it.type.id }
    val types = (tagOrder + map { it.type }).distinctBy { it.id }.filterNot { it.isCustom } + SpeakerType.CUSTOM
    return types.mapNotNull { t -> byId[t.id]?.let { t to it } }
}
