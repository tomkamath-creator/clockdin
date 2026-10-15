// app/src/main/java/com/vk/clockdin/data/db/ClockdinDatabase.kt
package com.vk.clockdin.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [MoodEntryEntity::class, MoodColorEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class ClockdinDatabase : RoomDatabase() {
    abstract fun moodEntryDao(): MoodEntryDao
    abstract fun moodColorDao(): MoodColorDao

    /** Seeds the default palette when the DB is first created. */
    class SeedCallback : Callback() {
        override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            super.onCreate(db)
            // We cannot access Room DAOs here (not ready yet). Use raw SQL.
            DEFAULT_MOOD_COLORS.forEach { color ->
                db.execSQL(
                    "INSERT INTO mood_colors (name, hex, category, sort_order, is_default) VALUES (?, ?, ?, ?, ?)",
                    arrayOf(color.name, color.hex, color.category.name, color.sortOrder, if (color.isDefault) 1 else 0)
                )
            }
        }
    }
}