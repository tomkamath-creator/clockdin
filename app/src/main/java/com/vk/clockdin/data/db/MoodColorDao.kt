// app/src/main/java/com/vk/clockdin/data/db/MoodColorDao.kt
package com.vk.clockdin.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodColorDao {

    @Query("SELECT * FROM mood_colors ORDER BY sort_order ASC")
    fun observeAll(): Flow<List<MoodColorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MoodColorEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<MoodColorEntity>)

    @Update
    suspend fun update(entity: MoodColorEntity)

    @Delete
    suspend fun delete(entity: MoodColorEntity)

    @Query("DELETE FROM mood_colors")
    suspend fun deleteAll()
}