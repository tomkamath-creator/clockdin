// Location: app/src/main/java/com/vk/clockdin/domain/repository/MoodColorRepository.kt
package com.vk.clockdin.domain.repository

import com.vk.clockdin.domain.model.MoodCategory
import com.vk.clockdin.domain.model.MoodColor
import kotlinx.coroutines.flow.Flow

interface MoodColorRepository {
    fun getAllColors(): Flow<List<MoodColor>>
    suspend fun getAllColorsList(): List<MoodColor>
    fun getColorsByCategory(category: MoodCategory): Flow<List<MoodColor>>
    suspend fun getColorById(id: Long): MoodColor?
    suspend fun addColor(color: MoodColor): Long
    suspend fun addColors(colors: List<MoodColor>)
    suspend fun updateColor(color: MoodColor)
    suspend fun updateColors(colors: List<MoodColor>)
    suspend fun deleteColor(color: MoodColor)
    suspend fun deleteColorById(id: Long)
    suspend fun resetToDefaults()
}
