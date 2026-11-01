package com.feldelysium.app.domain.model

data class TagData(
    val id: String,
    val name: String,
    val color: Long? = null,
    val sound: String? = null,
    val allowsChecks: Boolean = false,
)

data class SpeakerData(
    val id: String,
    val name: String,
    val color: Long? = null,
    val tagId: String? = null,
    val icon: String? = null,
)

data class SpeakerDocument(val tags: List<TagData> = emptyList(), val speakers: List<SpeakerData> = emptyList())
