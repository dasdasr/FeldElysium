
package com.example.feldelysium.data.feldpack

import kotlinx.serialization.Serializable

@Serializable
data class SpeakerPackDto(
    val format: String = FORMAT,
    val version: Int = CURRENT_VERSION,
    val speakers: List<SpeakerDto>,
) {
    companion object {
        const val FORMAT = "feldelysium.speakers"
        const val CURRENT_VERSION = 1
        const val MANIFEST_FILE = "pack.json"
        const val ICONS_DIRECTORY = "icons/"
    }
}

@Serializable
data class SpeakerDto(
    val id: String,
    val name: String,
    val color: String,
    val tag: TagDto? = null,
    /** Relative path inside the archive */
    val icon: String? = null,
)

@Serializable
data class TagDto(
    val id: String,
    val name: String,
)
