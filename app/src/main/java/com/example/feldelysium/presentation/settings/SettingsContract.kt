package com.example.feldelysium.presentation.settings

sealed interface SettingsEvent {
    data class SetLockAspectRatio16x9(val enabled: Boolean) : SettingsEvent
    data class SetPlaybackMoney(val value: String) : SettingsEvent
    data class SetPlaybackTime(val value: String) : SettingsEvent
    data class SetPlaybackDay(val value: String) : SettingsEvent
    data object ResetPlaybackUiText : SettingsEvent
}
