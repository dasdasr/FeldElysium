package com.example.feldelysium.data.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.appSettingsDataStore by preferencesDataStore(name = "app_settings")

/**
 * Single persistence boundary for [AppSettings].
 *
 * DataStore is process-wide for this file's delegate, so every
 * [SettingsRepository] shares the same store. Callers never touch keys:
 * they read [settings] and write through [update].
 */
class SettingsRepository(private val context: Context) {

    val settings: Flow<AppSettings> = context.appSettingsDataStore.data.map(::toAppSettings)

    suspend fun update(transform: (AppSettings) -> AppSettings) {
        context.appSettingsDataStore.edit { prefs ->
            val next = transform(toAppSettings(prefs))
            prefs[Keys.LOCK_ASPECT_RATIO_16_9] = next.lockAspectRatio16x9
        }
    }

    private fun toAppSettings(prefs: Preferences): AppSettings =
        AppSettings(
            lockAspectRatio16x9 = prefs[Keys.LOCK_ASPECT_RATIO_16_9] ?: true,
        )

    private object Keys {
        val LOCK_ASPECT_RATIO_16_9 = booleanPreferencesKey("lock_aspect_ratio_16_9")
    }
}
