// Location: app/src/main/java/com/vk/clockdin/data/repository/RoomMoodEntryRepository.kt
package com.vk.clockdin.data.repository

import com.vk.clockdin.data.local.dao.MoodEntryDao
import com.vk.clockdin.data.local.entity.MoodEntryEntity
import com.vk.clockdin.domain.model.MoodEntry
import com.vk.clockdin.domain.repository.MoodEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomMoodEntryRepository @Inject constructor(
    private val moodEntryDao: MoodEntryDao
) : MoodEntryRepository {

    override fun getAllEntries(): Flow<List<MoodEntry>> =
        moodEntryDao.getAllEntriesFlow().map { list -> list.map { it.toDomain() } }

    override fun getAllEntriesAscending(): Flow<List<MoodEntry>> =
        moodEntryDao.getAllEntriesAscendingFlow().map { list -> list.map { it.toDomain() } }

    override fun getEntryById(id: Long): Flow<MoodEntry?> =
        moodEntryDao.getEntryByIdFlow(id).map { it?.toDomain() }

    override suspend fun getEntryByIdDirect(id: Long): MoodEntry? =
        moodEntryDao.getEntryById(id)?.toDomain()

    override fun getEntriesBetweenDates(startDate: String, endDate: String): Flow<List<MoodEntry>> =
        moodEntryDao.getEntriesBetweenDatesFlow(startDate, endDate).map { list -> list.map { it.toDomain() } }

    override fun getEntriesByMonth(yearMonth: String): Flow<List<MoodEntry>> =
        moodEntryDao.getEntriesByMonthFlow(yearMonth).map { list -> list.map { it.toDomain() } }

    override fun getEntriesForDate(date: String): Flow<List<MoodEntry>> =
        moodEntryDao.getEntriesForDateFlow(date).map { list -> list.map { it.toDomain() } }

    override fun getAllDistinctDates(): Flow<List<String>> =
        moodEntryDao.getAllDistinctDatesFlow()

    override fun getEntryCount(): Flow<Int> =
        moodEntryDao.getEntryCountFlow()

    override suspend fun insertEntry(entry: MoodEntry): Long =
        moodEntryDao.insertEntry(MoodEntryEntity.fromDomain(entry))

    override suspend fun insertEntries(entries: List<MoodEntry>) =
        moodEntryDao.insertAll(entries.map { MoodEntryEntity.fromDomain(it) })

    override suspend fun updateEntry(entry: MoodEntry) =
        moodEntryDao.updateEntry(MoodEntryEntity.fromDomain(entry))

    override suspend fun deleteEntry(entry: MoodEntry) =
        moodEntryDao.deleteEntry(MoodEntryEntity.fromDomain(entry))

    override suspend fun deleteEntryById(id: Long) =
        moodEntryDao.deleteEntryById(id)

    override suspend fun deleteAllEntries() =
        moodEntryDao.deleteAllEntries()
}
