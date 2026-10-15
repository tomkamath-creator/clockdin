// app/src/main/java/com/vk/clockdin/data/repository/MoodEntryRepositoryImpl.kt
package com.vk.clockdin.data.repository

import com.vk.clockdin.data.db.MoodEntryDao
import com.vk.clockdin.data.db.MoodEntryEntity
import com.vk.clockdin.domain.model.MoodEntry
import com.vk.clockdin.domain.repository.MoodEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MoodEntryRepositoryImpl @Inject constructor(
    private val dao: MoodEntryDao
) : MoodEntryRepository {

    override fun observeAll(): Flow<List<MoodEntry>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeByMonth(yearMonth: String): Flow<List<MoodEntry>> =
        dao.observeByMonth(yearMonth).map { list -> list.map { it.toDomain() } }

    override fun observeById(id: Long): Flow<MoodEntry?> =
        dao.observeById(id).map { it?.toDomain() }

    override suspend fun insert(entry: MoodEntry): Long =
        dao.insert(MoodEntryEntity.fromDomain(entry))

    override suspend fun update(entry: MoodEntry) =
        dao.update(MoodEntryEntity.fromDomain(entry))

    override suspend fun delete(entry: MoodEntry) =
        dao.delete(MoodEntryEntity.fromDomain(entry))
}