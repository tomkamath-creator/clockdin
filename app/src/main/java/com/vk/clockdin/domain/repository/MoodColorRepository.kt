// app/src/main/java/com/vk/clockdin/domain/repository/MoodColorRepository.kt
package com.vk.clockdin.domain.repository

import com.vk.clockdin.domain.model.MoodColor
import kotlinx.coroutines.flow.Flow

interface MoodColorRepository {
    fun observeAll(): Flow<List<MoodColor>>
    suspend fun insert(color: MoodColor): Long
    suspend fun update(color: MoodColor)
    suspend fun delete(color: MoodColor)
    suspend fun resetToDefaults()
}