// Location: app/src/main/java/com/vk/clockdin/data/local/dao/MoodEntryDao.kt
package com.vk.clockdin.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vk.clockdin.data.local.entity.MoodEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodEntryDao {

    @Query("SELECT * FROM mood_entries ORDER BY createdAt DESC")
    fun getAllEntriesFlow(): Flow<List<MoodEntryEntity>>

    @Query("SELECT * FROM mood_entries ORDER BY createdAt ASC")
    fun getAllEntriesAscendingFlow(): Flow<List<MoodEntryEntity>>

    @Query("SELECT * FROM mood_entries WHERE id = :id LIMIT 1")
    fun getEntryByIdFlow(id: Long): Flow<MoodEntryEntity?>

    @Query("SELECT * FROM mood_entries WHERE id = :id LIMIT 1")
    suspend fun getEntryById(id: Long): MoodEntryEntity?

    @Query("SELECT * FROM mood_entries WHERE localDate >= :startDate AND localDate <= :endDate ORDER BY localDate ASC, createdAt ASC")
    fun getEntriesBetweenDatesFlow(startDate: String, endDate: String): Flow<List<MoodEntryEntity>>

    @Query("SELECT * FROM mood_entries WHERE localDate LIKE :yearMonth || '%' ORDER BY localDate ASC, createdAt ASC")
    fun getEntriesByMonthFlow(yearMonth: String): Flow<List<MoodEntryEntity>>

    @Query("SELECT * FROM mood_entries WHERE localDate = :date ORDER BY createdAt DESC")
    fun getEntriesForDateFlow(date: String): Flow<List<MoodEntryEntity>>

    @Query("SELECT DISTINCT localDate FROM mood_entries ORDER BY localDate DESC")
    fun getAllDistinctDatesFlow(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM mood_entries")
    fun getEntryCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM mood_entries")
    suspend fun getEntryCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: MoodEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<MoodEntryEntity>)

    @Update
    suspend fun updateEntry(entry: MoodEntryEntity)

    @Delete
    suspend fun deleteEntry(entry: MoodEntryEntity)

    @Query("DELETE FROM mood_entries WHERE id = :id")
    suspend fun deleteEntryById(id: Long)

    @Query("DELETE FROM mood_entries")
    suspend fun deleteAllEntries()
}
