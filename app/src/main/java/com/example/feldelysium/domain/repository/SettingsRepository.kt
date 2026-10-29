package com.example.feldelysium.domain.repository

import com.example.feldelysium.domain.model.settings.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun update(transform: (AppSettings) -> AppSettings)
}
