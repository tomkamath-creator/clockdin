// Location: app/src/main/java/com/vk/clockdin/domain/repository/MoodEntryRepository.kt
package com.vk.clockdin.domain.repository

import com.vk.clockdin.domain.model.MoodEntry
import kotlinx.coroutines.flow.Flow

interface MoodEntryRepository {
    fun getAllEntries(): Flow<List<MoodEntry>>
    fun getAllEntriesAscending(): Flow<List<MoodEntry>>
    fun getEntryById(id: Long): Flow<MoodEntry?>
    suspend fun getEntryByIdDirect(id: Long): MoodEntry?
    fun getEntriesBetweenDates(startDate: String, endDate: String): Flow<List<MoodEntry>>
    fun getEntriesByMonth(yearMonth: String): Flow<List<MoodEntry>>
    fun getEntriesForDate(date: String): Flow<List<MoodEntry>>
    fun getAllDistinctDates(): Flow<List<String>>
    fun getEntryCount(): Flow<Int>
    suspend fun insertEntry(entry: MoodEntry): Long
    suspend fun insertEntries(entries: List<MoodEntry>)
    suspend fun updateEntry(entry: MoodEntry)
    suspend fun deleteEntry(entry: MoodEntry)
    suspend fun deleteEntryById(id: Long)
    suspend fun deleteAllEntries()
}
