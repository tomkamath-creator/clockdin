// Location: app/src/main/java/com/vk/clockdin/data/local/ClockdinDatabase.kt
package com.vk.clockdin.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.vk.clockdin.data.local.converter.Converters
import com.vk.clockdin.data.local.dao.MoodColorDao
import com.vk.clockdin.data.local.dao.MoodEntryDao
import com.vk.clockdin.data.local.entity.MoodColorEntity
import com.vk.clockdin.data.local.entity.MoodEntryEntity

@Database(
    entities = [
        MoodEntryEntity::class,
        MoodColorEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ClockdinDatabase : RoomDatabase() {

    abstract fun moodEntryDao(): MoodEntryDao
    abstract fun moodColorDao(): MoodColorDao

    companion object {
        const val DATABASE_NAME = "clockdin_db"
    }
}
