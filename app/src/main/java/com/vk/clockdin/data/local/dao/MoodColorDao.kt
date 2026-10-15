// Location: app/src/main/java/com/vk/clockdin/data/local/dao/MoodColorDao.kt
package com.vk.clockdin.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vk.clockdin.data.local.entity.MoodColorEntity
import com.vk.clockdin.domain.model.MoodCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodColorDao {

    @Query("SELECT * FROM mood_colors ORDER BY category ASC, sortOrder ASC")
    fun getAllColorsFlow(): Flow<List<MoodColorEntity>>

    @Query("SELECT * FROM mood_colors ORDER BY category ASC, sortOrder ASC")
    suspend fun getAllColors(): List<MoodColorEntity>

    @Query("SELECT * FROM mood_colors WHERE category = :category ORDER BY sortOrder ASC")
    fun getColorsByCategoryFlow(category: MoodCategory): Flow<List<MoodColorEntity>>

    @Query("SELECT * FROM mood_colors WHERE id = :id LIMIT 1")
    suspend fun getColorById(id: Long): MoodColorEntity?

    @Query("SELECT COUNT(*) FROM mood_colors")
    suspend fun getColorCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertColor(color: MoodColorEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertColors(colors: List<MoodColorEntity>)

    @Update
    suspend fun updateColor(color: MoodColorEntity)

    @Update
    suspend fun updateColors(colors: List<MoodColorEntity>)

    @Delete
    suspend fun deleteColor(color: MoodColorEntity)

    @Query("DELETE FROM mood_colors WHERE id = :id")
    suspend fun deleteColorById(id: Long)

    @Query("DELETE FROM mood_colors")
    suspend fun deleteAllColors()
}
