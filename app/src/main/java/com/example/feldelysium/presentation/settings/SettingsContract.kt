package com.example.feldelysium.presentation.settings

import com.example.feldelysium.data.settings.AppSettings
import com.example.feldelysium.data.settings.BackgroundKind
import com.example.feldelysium.data.settings.BarIconState
import com.example.feldelysium.data.settings.BarSlot
import com.example.feldelysium.data.settings.InventoryIcon
import com.example.feldelysium.data.settings.InventorySlot
import com.example.feldelysium.data.settings.SamplesMode

sealed interface SettingsEvent {
    data class SetLockAspectRatio16x9(val enabled: Boolean) : SettingsEvent
    data class SetPlaybackMoney(val value: String) : SettingsEvent
    data class SetPlaybackTime(val value: String) : SettingsEvent
    data class SetPlaybackDay(val value: String) : SettingsEvent
    data object ResetPlaybackUiText : SettingsEvent
    data class SetBackgroundKind(val kind: BackgroundKind) : SettingsEvent
    data class SetBackgroundColor(val argb: Long) : SettingsEvent
    data class SetBackgroundMedia(val uri: String, val isVideo: Boolean) : SettingsEvent
    data class SetBackgroundVideoSound(val enabled: Boolean) : SettingsEvent
    data class SetBackgroundVideoFill(val enabled: Boolean) : SettingsEvent
    data object ResetBackground : SettingsEvent
    data class SetSamplesMode(val mode: SamplesMode) : SettingsEvent
    data class SetBarIconState(val slot: BarSlot, val state: BarIconState) : SettingsEvent
    data object ResetBarIcons : SettingsEvent
    data class SetInventoryIcon(val slot: InventorySlot, val icon: InventoryIcon) : SettingsEvent
}

@Suppress("CyclomaticComplexMethod") // flat exhaustive when: one branch per SettingsEvent
internal fun AppSettings.apply(event: SettingsEvent): AppSettings {
    val d = AppSettings()
    return when (event) {
        is SettingsEvent.SetLockAspectRatio16x9 -> copy(lockAspectRatio16x9 = event.enabled)

        is SettingsEvent.SetPlaybackMoney -> copy(playbackMoney = event.value)

        is SettingsEvent.SetPlaybackTime -> copy(playbackTime = event.value)

        is SettingsEvent.SetPlaybackDay -> copy(playbackDay = event.value)

        SettingsEvent.ResetPlaybackUiText ->
            copy(playbackMoney = d.playbackMoney, playbackTime = d.playbackTime, playbackDay = d.playbackDay)

        is SettingsEvent.SetBackgroundKind -> copy(backgroundKind = event.kind)

        is SettingsEvent.SetBackgroundColor ->
            copy(backgroundColorArgb = event.argb, backgroundKind = BackgroundKind.COLOR)

        is SettingsEvent.SetBackgroundMedia ->
            copy(
                backgroundKind = BackgroundKind.MEDIA,
                backgroundMediaUri = event.uri,
                backgroundMediaIsVideo = event.isVideo,
            )

        is SettingsEvent.SetBackgroundVideoSound -> copy(backgroundVideoSound = event.enabled)

        is SettingsEvent.SetBackgroundVideoFill -> copy(backgroundVideoFill = event.enabled)

        SettingsEvent.ResetBackground -> copy(
            backgroundKind = d.backgroundKind,
            backgroundColorArgb = d.backgroundColorArgb,
            backgroundMediaUri = d.backgroundMediaUri,
            backgroundMediaIsVideo = d.backgroundMediaIsVideo,
            backgroundVideoSound = d.backgroundVideoSound,
            backgroundVideoFill = d.backgroundVideoFill,
        )

        is SettingsEvent.SetSamplesMode -> copy(samplesMode = event.mode)

        is SettingsEvent.SetBarIconState -> copy(barIcons = barIcons + (event.slot to event.state))

        is SettingsEvent.SetInventoryIcon -> copy(inventoryIcons = inventoryIcons + (event.slot to event.icon))

        SettingsEvent.ResetBarIcons -> copy(barIcons = d.barIcons, inventoryIcons = d.inventoryIcons)
    }
}
