package com.example.feldelysium.presentation.settings

import androidx.annotation.StringRes
import com.example.feldelysium.domain.model.settings.AppSettings
import com.example.feldelysium.domain.model.settings.BackgroundKind
import com.example.feldelysium.domain.model.settings.BarSlot
import com.example.feldelysium.domain.model.settings.InventorySlot
import com.example.feldelysium.domain.model.settings.TapMode
import com.example.feldelysium.domain.model.settings.next

sealed interface SettingsEvent {
    data class SetLockAspectRatio16x9(val enabled: Boolean) : SettingsEvent
    data class SetAutoPlay(val enabled: Boolean) : SettingsEvent
    data class ShowContinueButton(val enabled: Boolean) : SettingsEvent
    data object CycleAutoPlaySpeed : SettingsEvent
    data object CycleTapMode : SettingsEvent
    data class SetFontChoice(val choice: String) : SettingsEvent

    data class SetPlaybackMoney(val value: String) : SettingsEvent
    data class SetPlaybackTime(val value: String) : SettingsEvent
    data class SetPlaybackDay(val value: String) : SettingsEvent
    data object ResetPlaybackUiText : SettingsEvent

    data class SetExportEnabled(val enabled: Boolean) : SettingsEvent
    data class SetExportAudio(val enabled: Boolean) : SettingsEvent

    data class SetBackgroundKind(val kind: BackgroundKind) : SettingsEvent
    data class SetBackgroundColor(val argb: Long) : SettingsEvent
    data class SetBackgroundMedia(val uri: String, val isVideo: Boolean) : SettingsEvent
    data class SetBackgroundVideoSound(val enabled: Boolean) : SettingsEvent
    data class SetBackgroundVideoFill(val enabled: Boolean) : SettingsEvent
    data object ResetBackground : SettingsEvent

    data object CycleSamplesMode : SettingsEvent

    data class CycleBarIcon(val slot: BarSlot) : SettingsEvent
    data class CycleInventoryIcon(val slot: InventorySlot) : SettingsEvent
    data object ResetBarIcons : SettingsEvent
}

internal fun AppSettings.apply(event: SettingsEvent): AppSettings = reduce(event).withConsistentTapMode()

private fun AppSettings.withConsistentTapMode(): AppSettings =
    if (!autoPlay && tapMode == TapMode.NOWHERE) copy(tapMode = TapMode.WHOLE_SCREEN) else this

@Suppress("CyclomaticComplexMethod") // flat exhaustive when: one branch per SettingsEvent
private fun AppSettings.reduce(event: SettingsEvent): AppSettings {
    val d = AppSettings()
    return when (event) {
        is SettingsEvent.SetLockAspectRatio16x9 -> copy(lockAspectRatio16x9 = event.enabled)

        is SettingsEvent.SetAutoPlay -> copy(autoPlay = event.enabled)

        is SettingsEvent.ShowContinueButton -> copy(showContinueButton = event.enabled)

        SettingsEvent.CycleAutoPlaySpeed -> copy(autoPlaySpeed = autoPlaySpeed.next())

        SettingsEvent.CycleTapMode -> copy(tapMode = tapMode.next(autoPlay))

        is SettingsEvent.SetFontChoice -> copy(fontChoice = event.choice)

        is SettingsEvent.SetPlaybackMoney -> copy(playbackMoney = event.value)

        is SettingsEvent.SetPlaybackTime -> copy(playbackTime = event.value)

        is SettingsEvent.SetPlaybackDay -> copy(playbackDay = event.value)

        SettingsEvent.ResetPlaybackUiText ->
            copy(playbackMoney = d.playbackMoney, playbackTime = d.playbackTime, playbackDay = d.playbackDay)

        is SettingsEvent.SetExportEnabled -> copy(exportEnabled = event.enabled)

        is SettingsEvent.SetExportAudio -> copy(exportAudio = event.enabled)

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

        SettingsEvent.CycleSamplesMode -> copy(samplesMode = samplesMode.next())

        is SettingsEvent.CycleBarIcon -> {
            val current = barIcons[event.slot] ?: event.slot.defaultState
            copy(barIcons = barIcons + (event.slot to current.next()))
        }

        is SettingsEvent.CycleInventoryIcon -> {
            val current = inventoryIcons[event.slot] ?: event.slot.defaultIcon
            copy(inventoryIcons = inventoryIcons + (event.slot to current.next()))
        }

        SettingsEvent.ResetBarIcons -> copy(barIcons = d.barIcons, inventoryIcons = d.inventoryIcons)
    }
}

sealed interface SettingsEffect {
    data class ShowMessage(@StringRes val messageRes: Int) : SettingsEffect
}

enum class SettingsDialog {
    PlaybackUiText,
    BarIcons,
    Background,
    Font,
    Tags,
    Language,
}
