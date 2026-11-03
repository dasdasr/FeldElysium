package com.feldelysium.app.data.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.feldelysium.app.domain.model.settings.AppSettings
import com.feldelysium.app.domain.model.settings.AutoPlaySpeed
import com.feldelysium.app.domain.model.settings.BackgroundKind
import com.feldelysium.app.domain.model.settings.BarIconState
import com.feldelysium.app.domain.model.settings.BarSlot
import com.feldelysium.app.domain.model.settings.InventoryIcon
import com.feldelysium.app.domain.model.settings.InventorySlot
import com.feldelysium.app.domain.model.settings.SamplesMode
import com.feldelysium.app.domain.model.settings.TapMode
import com.feldelysium.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.appSettingsDataStore by preferencesDataStore(name = "app_settings")

class DataStoreSettingsRepository(private val context: Context) : SettingsRepository {

    override val settings: Flow<AppSettings> = context.appSettingsDataStore.data.map(::toAppSettings)

    override suspend fun update(transform: (AppSettings) -> AppSettings) {
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
            prefs[Keys.EXPORT_ENABLED] = next.exportEnabled
            prefs[Keys.EXPORT_AUDIO] = next.exportAudio
            prefs[Keys.AUTO_PLAY] = next.autoPlay
            prefs[Keys.AUTO_PLAY_SPEED] = next.autoPlaySpeed.name
            prefs[Keys.SHOW_CONTINUE_BUTTON] = next.showContinueButton
            prefs[Keys.TAP_MODE] = next.tapMode.name
            next.barIcons.forEach { (slot, state) -> prefs[barKey(slot)] = state.name }
            next.inventoryIcons.forEach { (slot, icon) -> prefs[invKey(slot)] = icon.name }
            prefs[Keys.CUSTOM_FONT_FILE] = next.customFontFile
            prefs[Keys.CUSTOM_FONT_NAME] = next.customFontName
            prefs[Keys.FONT_CHOICE] = next.fontChoice
        }
    }

    @Suppress("CyclomaticComplexMethod")
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
        exportEnabled = prefs[Keys.EXPORT_ENABLED] ?: false,
        exportAudio = prefs[Keys.EXPORT_AUDIO] ?: false ,
        autoPlay = prefs[Keys.AUTO_PLAY] ?: false,
        autoPlaySpeed = prefs[Keys.AUTO_PLAY_SPEED]
            ?.let { runCatching { AutoPlaySpeed.valueOf(it) }.getOrNull() }
            ?: AutoPlaySpeed.NORMAL,
        tapMode = prefs[Keys.TAP_MODE]
            ?.let { runCatching { TapMode.valueOf(it) }.getOrNull() }
            ?: TapMode.WHOLE_SCREEN,
        barIcons = BarSlot.entries.associateWith { slot ->
            prefs[barKey(slot)]
                ?.let { runCatching { BarIconState.valueOf(it) }.getOrNull() }
                ?: slot.defaultState
        },
        inventoryIcons = InventorySlot.entries.associateWith { slot ->
            prefs[invKey(slot)]
                ?.let { runCatching { InventoryIcon.valueOf(it) }.getOrNull() }
                ?: slot.defaultIcon
        },
        showContinueButton = prefs[Keys.SHOW_CONTINUE_BUTTON] ?: true,
        customFontFile = prefs[Keys.CUSTOM_FONT_FILE] ?: "",
        customFontName = prefs[Keys.CUSTOM_FONT_NAME] ?: "",
        fontChoice = prefs[Keys.FONT_CHOICE] ?: "",
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
        val EXPORT_ENABLED = booleanPreferencesKey("export_enabled")
        val EXPORT_AUDIO = booleanPreferencesKey("export_audio")
        val AUTO_PLAY = booleanPreferencesKey("auto_play")
        val AUTO_PLAY_SPEED = stringPreferencesKey("auto_play_speed")
        val SHOW_CONTINUE_BUTTON = booleanPreferencesKey("show_continue_button")
        val TAP_MODE = stringPreferencesKey("tap_mode")
        val CUSTOM_FONT_FILE = stringPreferencesKey("custom_font_file")
        val CUSTOM_FONT_NAME = stringPreferencesKey("custom_font_name")
        val FONT_CHOICE = stringPreferencesKey("font_choice")
    }

    private fun barKey(slot: BarSlot) = stringPreferencesKey("bar_icon_${slot.name.lowercase()}")
    private fun invKey(slot: InventorySlot) = stringPreferencesKey("bar_inv_${slot.name.lowercase()}")
}
