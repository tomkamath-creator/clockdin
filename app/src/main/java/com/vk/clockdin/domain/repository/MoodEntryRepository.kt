// app/src/main/java/com/vk/clockdin/domain/repository/MoodEntryRepository.kt
package com.vk.clockdin.domain.repository

import com.vk.clockdin.domain.model.MoodEntry
import kotlinx.coroutines.flow.Flow

interface MoodEntryRepository {
    /** All entries, newest first. */
    fun observeAll(): Flow<List<MoodEntry>>

    /** Entries whose localDate falls within the ISO month string, e.g. "2024-11". */
    fun observeByMonth(yearMonth: String): Flow<List<MoodEntry>>

    /** Single entry by primary key, or null. */
    fun observeById(id: Long): Flow<MoodEntry?>

    suspend fun insert(entry: MoodEntry): Long

    suspend fun update(entry: MoodEntry)

    suspend fun delete(entry: MoodEntry)
}