package com.example.feldelysium.data.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.appSettingsDataStore by preferencesDataStore(name = "app_settings")

class SettingsRepository(private val context: Context) {

    val settings: Flow<AppSettings> = context.appSettingsDataStore.data.map(::toAppSettings)

    suspend fun update(transform: (AppSettings) -> AppSettings) {
        context.appSettingsDataStore.edit { prefs ->
            val next = transform(toAppSettings(prefs))
            prefs[Keys.LOCK_ASPECT_RATIO_16_9] = next.lockAspectRatio16x9
            prefs[Keys.PLAYBACK_MONEY] = next.playbackMoney
            prefs[Keys.PLAYBACK_TIME] = next.playbackTime
            prefs[Keys.PLAYBACK_DAY] = next.playbackDay
            prefs[Keys.BACKGROUND_KIND] = next.backgroundKind.name
            prefs[Keys.BACKGROUND_COLOR] = next.backgroundColorArgb
            prefs[Keys.BACKGROUND_MEDIA_URI] = next.backgroundMediaUri
            prefs[Keys.BACKGROUND_MEDIA_IS_VIDEO] = next.backgroundMediaIsVideo
            prefs[Keys.BACKGROUND_VIDEO_SOUND] = next.backgroundVideoSound
            prefs[Keys.BACKGROUND_VIDEO_FILL] = next.backgroundVideoFill
            prefs[Keys.SAMPLES_MODE] = next.samplesMode.name
            prefs[Keys.PACK_IMPORTED] = next.packImported
        }
    }

    private fun toAppSettings(prefs: Preferences): AppSettings = AppSettings(
        lockAspectRatio16x9 = prefs[Keys.LOCK_ASPECT_RATIO_16_9] ?: true,
        playbackMoney = prefs[Keys.PLAYBACK_MONEY] ?: "40.95",
        playbackTime = prefs[Keys.PLAYBACK_TIME] ?: "16:36",
        playbackDay = prefs[Keys.PLAYBACK_DAY] ?: "Day 3",
        backgroundKind = prefs[Keys.BACKGROUND_KIND]
            ?.let { runCatching { BackgroundKind.valueOf(it) }.getOrNull() }
            ?: BackgroundKind.COLOR,
        backgroundColorArgb = prefs[Keys.BACKGROUND_COLOR] ?: AppSettings.DEFAULT_BACKGROUND_COLOR,
        backgroundMediaUri = prefs[Keys.BACKGROUND_MEDIA_URI] ?: "",
        backgroundMediaIsVideo = prefs[Keys.BACKGROUND_MEDIA_IS_VIDEO] ?: false,
        backgroundVideoSound = prefs[Keys.BACKGROUND_VIDEO_SOUND] ?: false,
        backgroundVideoFill = prefs[Keys.BACKGROUND_VIDEO_FILL] ?: true,
        samplesMode = prefs[Keys.SAMPLES_MODE]
            ?.let { runCatching { SamplesMode.valueOf(it) }.getOrNull() }
            ?: SamplesMode.AUTO,
        packImported = prefs[Keys.PACK_IMPORTED] ?: false,
    )

    private object Keys {
        val LOCK_ASPECT_RATIO_16_9 = booleanPreferencesKey("lock_aspect_ratio_16_9")
        val PLAYBACK_MONEY = stringPreferencesKey("playback_money")
        val PLAYBACK_TIME = stringPreferencesKey("playback_time")
        val PLAYBACK_DAY = stringPreferencesKey("playback_day")
        val BACKGROUND_KIND = stringPreferencesKey("background_kind")
        val BACKGROUND_COLOR = longPreferencesKey("background_color")
        val BACKGROUND_MEDIA_URI = stringPreferencesKey("background_media_uri")
        val BACKGROUND_MEDIA_IS_VIDEO = booleanPreferencesKey("background_media_is_video")
        val BACKGROUND_VIDEO_SOUND = booleanPreferencesKey("background_video_sound")
        val BACKGROUND_VIDEO_FILL = booleanPreferencesKey("background_video_fill")
        val SAMPLES_MODE = stringPreferencesKey("samples_mode")
        val PACK_IMPORTED = booleanPreferencesKey("pack_imported")
    }
}
