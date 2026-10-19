// Location: app/src/main/java/com/vk/clockdin/data/repository/RoomMoodColorRepository.kt
package com.vk.clockdin.data.repository

import com.vk.clockdin.data.local.DefaultPalette
import com.vk.clockdin.data.local.dao.MoodColorDao
import com.vk.clockdin.data.local.entity.MoodColorEntity
import com.vk.clockdin.domain.model.MoodCategory
import com.vk.clockdin.domain.model.MoodColor
import com.vk.clockdin.domain.repository.MoodColorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomMoodColorRepository @Inject constructor(
    private val moodColorDao: MoodColorDao
) : MoodColorRepository {

    override fun getAllColors(): Flow<List<MoodColor>> =
        moodColorDao.getAllColorsFlow().map { list ->
            if (list.isEmpty()) {
                DefaultPalette.defaultColors
            } else {
                list.map { it.toDomain() }
            }
        }

    override suspend fun getAllColorsList(): List<MoodColor> {
        val list = moodColorDao.getAllColors()
        return if (list.isEmpty()) {
            DefaultPalette.defaultColors
        } else {
            list.map { it.toDomain() }
        }
    }

    override fun getColorsByCategory(category: MoodCategory): Flow<List<MoodColor>> =
        moodColorDao.getColorsByCategoryFlow(category).map { list -> list.map { it.toDomain() } }

    override suspend fun getColorById(id: Long): MoodColor? =
        moodColorDao.getColorById(id)?.toDomain()

    override suspend fun addColor(color: MoodColor): Long {
        if (moodColorDao.getColorCount() == 0) {
            moodColorDao.insertColors(DefaultPalette.defaultColors.map { MoodColorEntity.fromDomain(it) })
        }
        return moodColorDao.insertColor(MoodColorEntity.fromDomain(color))
    }

    override suspend fun addColors(colors: List<MoodColor>) =
        moodColorDao.insertColors(colors.map { MoodColorEntity.fromDomain(it) })

    override suspend fun updateColor(color: MoodColor) =
        moodColorDao.updateColor(MoodColorEntity.fromDomain(color))

    override suspend fun updateColors(colors: List<MoodColor>) =
        moodColorDao.updateColors(colors.map { MoodColorEntity.fromDomain(it) })

    override suspend fun deleteColor(color: MoodColor) =
        moodColorDao.deleteColor(MoodColorEntity.fromDomain(color))

    override suspend fun deleteColorById(id: Long) =
        moodColorDao.deleteColorById(id)

    override suspend fun resetToDefaults() {
        moodColorDao.deleteAllColors()
        moodColorDao.insertColors(DefaultPalette.defaultColors.map { MoodColorEntity.fromDomain(it) })
    }
}
