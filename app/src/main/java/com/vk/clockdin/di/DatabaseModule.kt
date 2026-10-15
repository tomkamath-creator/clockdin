// Location: app/src/main/java/com/vk/clockdin/di/DatabaseModule.kt
package com.vk.clockdin.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.vk.clockdin.data.local.ClockdinDatabase
import com.vk.clockdin.data.local.DefaultPalette
import com.vk.clockdin.data.local.dao.MoodColorDao
import com.vk.clockdin.data.local.dao.MoodEntryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideClockdinDatabase(
        @ApplicationContext context: Context
    ): ClockdinDatabase {
        return Room.databaseBuilder(
            context,
            ClockdinDatabase::class.java,
            ClockdinDatabase.DATABASE_NAME
        ).addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Transactional direct seed of default palette during initial database creation
                for (color in DefaultPalette.defaultColors) {
                    val isDefaultInt = if (color.isDefault) 1 else 0
                    db.execSQL(
                        "INSERT INTO mood_colors (name, hex, category, sortOrder, isDefault) VALUES (?, ?, ?, ?, ?)",
                        arrayOf(color.name, color.hex, color.category.name, color.sortOrder, isDefaultInt)
                    )
                }
            }
        }).build()
    }

    @Provides
    fun provideMoodEntryDao(database: ClockdinDatabase): MoodEntryDao =
        database.moodEntryDao()

    @Provides
    fun provideMoodColorDao(database: ClockdinDatabase): MoodColorDao =
        database.moodColorDao()
}
