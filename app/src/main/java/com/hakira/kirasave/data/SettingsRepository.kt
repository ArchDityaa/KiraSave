package com.hakira.kirasave.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "kira_settings")

data class AppSettings(
    val theme: String = "DARK",
    val defaultQuality: String = "HD",
    val wifiOnly: Boolean = false,
    val maxConcurrent: Int = 2,
    val autoPaste: Boolean = true,
    val notifications: Boolean = true,
    val onboardingDone: Boolean = false
)

class SettingsRepository(private val context: Context) {
    private object Keys {
        val theme = stringPreferencesKey("theme")
        val quality = stringPreferencesKey("default_quality")
        val wifiOnly = booleanPreferencesKey("wifi_only")
        val concurrent = intPreferencesKey("max_concurrent")
        val autoPaste = booleanPreferencesKey("auto_paste")
        val notifications = booleanPreferencesKey("notifications")
        val onboarding = booleanPreferencesKey("onboarding_done")
    }

    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { values ->
        AppSettings(
            theme = values[Keys.theme] ?: "DARK",
            defaultQuality = values[Keys.quality] ?: "HD",
            wifiOnly = values[Keys.wifiOnly] ?: false,
            maxConcurrent = (values[Keys.concurrent] ?: 2).coerceIn(1, 4),
            autoPaste = values[Keys.autoPaste] ?: true,
            notifications = values[Keys.notifications] ?: true,
            onboardingDone = values[Keys.onboarding] ?: false
        )
    }

    suspend fun setTheme(value: String) = context.settingsDataStore.edit { it[Keys.theme] = value }
    suspend fun setDefaultQuality(value: String) =
        context.settingsDataStore.edit { it[Keys.quality] = value }

    suspend fun setWifiOnly(value: Boolean) =
        context.settingsDataStore.edit { it[Keys.wifiOnly] = value }

    suspend fun setMaxConcurrent(value: Int) =
        context.settingsDataStore.edit { it[Keys.concurrent] = value.coerceIn(1, 4) }

    suspend fun setAutoPaste(value: Boolean) =
        context.settingsDataStore.edit { it[Keys.autoPaste] = value }

    suspend fun setNotifications(value: Boolean) =
        context.settingsDataStore.edit { it[Keys.notifications] = value }

    suspend fun setOnboardingDone(value: Boolean) =
        context.settingsDataStore.edit { it[Keys.onboarding] = value }
}
