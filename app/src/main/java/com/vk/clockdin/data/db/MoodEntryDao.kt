// app/src/main/java/com/vk/clockdin/data/db/MoodEntryDao.kt
package com.vk.clockdin.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodEntryDao {

    @Query("SELECT * FROM mood_entries ORDER BY created_at DESC")
    fun observeAll(): Flow<List<MoodEntryEntity>>

    /**
     * Entries where local_date starts with the given yearMonth prefix, e.g. "2024-11".
     */
    @Query("SELECT * FROM mood_entries WHERE local_date LIKE :yearMonth || '-%' ORDER BY created_at DESC")
    fun observeByMonth(yearMonth: String): Flow<List<MoodEntryEntity>>

    @Query("SELECT * FROM mood_entries WHERE id = :id")
    fun observeById(id: Long): Flow<MoodEntryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MoodEntryEntity): Long

    @Update
    suspend fun update(entity: MoodEntryEntity)

    @Delete
    suspend fun delete(entity: MoodEntryEntity)

    /** All local_date strings (for streak computation). Cheapest possible query. */
    @Query("SELECT local_date FROM mood_entries ORDER BY local_date DESC")
    fun observeLocalDates(): Flow<List<String>>
}