// app/src/main/java/com/vk/clockdin/data/repository/MoodColorRepositoryImpl.kt
package com.vk.clockdin.data.repository

import com.vk.clockdin.data.db.DEFAULT_MOOD_COLORS
import com.vk.clockdin.data.db.MoodColorDao
import com.vk.clockdin.data.db.MoodColorEntity
import com.vk.clockdin.domain.model.MoodColor
import com.vk.clockdin.domain.repository.MoodColorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MoodColorRepositoryImpl @Inject constructor(
    private val dao: MoodColorDao
) : MoodColorRepository {

    override fun observeAll(): Flow<List<MoodColor>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun insert(color: MoodColor): Long =
        dao.insert(MoodColorEntity.fromDomain(color))

    override suspend fun update(color: MoodColor) =
        dao.update(MoodColorEntity.fromDomain(color))

    override suspend fun delete(color: MoodColor) =
        dao.delete(MoodColorEntity.fromDomain(color))

    override suspend fun resetToDefaults() {
        dao.deleteAll()
        dao.insertAll(DEFAULT_MOOD_COLORS)
    }
}