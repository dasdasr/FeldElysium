package com.example.feldelysium.presentation.shared.language

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import com.example.feldelysium.R
import java.util.Locale

enum class AppLanguage(val tag: String?, val nativeName: String?) {
    SYSTEM(null, null),
    ENGLISH("en", "English"),
    GERMAN("de", "Deutsch"),
    FRENCH("fr", "Français"),
    SPANISH("es", "Español"),
    PORTUGUESE_BR("pt-BR", "Português"),
    POLISH("pl", "Polski"),
    UKRAINIAN("uk", "Українська"),
    BELARUSIAN("be", "Беларуская"),
    RUSSIAN("ru", "Русский"),
    TURKISH("tr", "Türkçe"),
    ;

    companion object {
        fun current(): AppLanguage {
            val language = AppCompatDelegate.getApplicationLocales()
                .takeUnless { it.isEmpty }?.get(0)?.language
            return entries.firstOrNull {
                it.tag != null && Locale.forLanguageTag(it.tag).language == language
            } ?: SYSTEM
        }

        fun apply(language: AppLanguage) {
            AppCompatDelegate.setApplicationLocales(
                if (language.tag == null) {
                    LocaleListCompat.getEmptyLocaleList()
                } else {
                    LocaleListCompat.forLanguageTags(language.tag)
                },
            )
        }
    }
}

@Composable
fun AppLanguage.displayName(): String = nativeName ?: stringResource(R.string.settings_language_system)
