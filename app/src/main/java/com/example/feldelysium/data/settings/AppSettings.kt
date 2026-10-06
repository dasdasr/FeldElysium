package com.example.feldelysium.data.settings

/**
 * Snapshot of every persisted app setting.
 *
 * To add a setting:
 * 1. Add a field here with its default.
 * 2. Map it in [SettingsRepository] (one key, one read, one write).
 * 3. Add a [com.example.feldelysium.presentation.settings.SettingsEvent] and a row on Settings.
 * 4. Read the field from the activity-scoped SettingsViewModel wherever it should apply.
 *
 * Defaults here are what the UI shows before DataStore emits, and what a missing
 * key falls back to. Keep them identical to the repository fallbacks.
 */
data class AppSettings(
    val lockAspectRatio16x9: Boolean = true,
)
