// Location: app/src/main/java/com/vk/clockdin/domain/repository/SettingsRepository.kt
package com.vk.clockdin.domain.repository

import com.vk.clockdin.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settingsFlow: Flow<UserSettings>
    suspend fun setHapticsEnabled(enabled: Boolean)
    suspend fun setReminderEnabled(enabled: Boolean)
    suspend fun setReminderTime(hour: Int, minute: Int)
    suspend fun setLastBackupTimestamp(timestamp: Long)
}
