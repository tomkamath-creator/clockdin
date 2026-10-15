// app/src/main/java/com/vk/clockdin/data/datastore/SettingsRepositoryImpl.kt
package com.vk.clockdin.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.vk.clockdin.domain.model.AppSettings
import com.vk.clockdin.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "clockdin_settings")

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private object Keys {
        val HAPTICS = booleanPreferencesKey("haptics_enabled")
        val REMINDER = booleanPreferencesKey("reminder_enabled")
        val REMINDER_TIME = stringPreferencesKey("reminder_time")
        val LAST_BACKUP = longPreferencesKey("last_backup_ts")
    }

    override fun observe(): Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            hapticsEnabled = prefs[Keys.HAPTICS] ?: true,
            reminderEnabled = prefs[Keys.REMINDER] ?: false,
            reminderTime = prefs[Keys.REMINDER_TIME] ?: "20:00",
            lastBackupTimestamp = prefs[Keys.LAST_BACKUP] ?: 0L
        )
    }

    override suspend fun update(settings: AppSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.HAPTICS] = settings.hapticsEnabled
            prefs[Keys.REMINDER] = settings.reminderEnabled
            prefs[Keys.REMINDER_TIME] = settings.reminderTime
            prefs[Keys.LAST_BACKUP] = settings.lastBackupTimestamp
        }
    }
}