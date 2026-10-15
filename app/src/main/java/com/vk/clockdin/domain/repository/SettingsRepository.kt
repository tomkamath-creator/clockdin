// app/src/main/java/com/vk/clockdin/domain/repository/SettingsRepository.kt
package com.vk.clockdin.domain.repository

import com.vk.clockdin.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observe(): Flow<AppSettings>
    suspend fun update(settings: AppSettings)
}