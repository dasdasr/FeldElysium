package com.example.feldelysium.presentation.settings

import com.example.feldelysium.data.settings.BackgroundKind

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
}
