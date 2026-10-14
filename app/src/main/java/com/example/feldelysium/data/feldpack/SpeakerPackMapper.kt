package com.example.feldelysium.data.feldpack

import com.example.feldelysium.domain.model.Speaker
import com.example.feldelysium.domain.model.SpeakerColor
import com.example.feldelysium.domain.model.SpeakerType
import java.util.Locale

internal fun Speaker.toDto(iconPath: String?): SpeakerDto =
    SpeakerDto(
        id = id,
        name = name,
        color = "%08X".format(
            Locale.ROOT,
            colorArgb and 0xFFFFFFFFL,
        ),
        tag = type
            .takeIf { it != SpeakerType.CUSTOM }
            ?.let { TagDto(id = it.id, name = it.name) },
        icon = iconPath,
    )

internal fun SpeakerDto.toSpeaker(iconUri: String?): Speaker {
    val normalizedHex = color
        .trim()
        .removePrefix("#")
        .let { if (it.length == 6) "FF$it" else it }

    val parsedColor = normalizedHex
        .takeIf { it.matches(Regex("[0-9a-fA-F]{8}")) }
        ?.toLongOrNull(16)
        ?: SpeakerColor.DEFAULT.argb

    val restoredType = tag?.let {
        SpeakerType.fromStored(it.id, it.name)
    } ?: SpeakerType.CUSTOM

    return Speaker(
        id = id,
        name = name.trim(),
        colorArgb = SpeakerColor.snap(parsedColor),
        customIconUri = iconUri,
        type = restoredType,
    )
}
