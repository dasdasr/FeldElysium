package com.example.feldelysium.presentation.settings

sealed interface SettingsEvent {
    data class SetLockAspectRatio16x9(val enabled: Boolean) : SettingsEvent
}
