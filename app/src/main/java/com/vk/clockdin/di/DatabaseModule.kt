// app/src/main/java/com/vk/clockdin/di/DatabaseModule.kt
package com.vk.clockdin.di

import android.content.Context
import androidx.room.Room
import com.vk.clockdin.data.db.ClockdinDatabase
import com.vk.clockdin.data.db.MoodColorDao
import com.vk.clockdin.data.db.MoodEntryDao
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
    fun provideDatabase(@ApplicationContext context: Context): ClockdinDatabase =
        Room.databaseBuilder(context, ClockdinDatabase::class.java, "clockdin.db")
            .addCallback(ClockdinDatabase.SeedCallback())
            .build()

    @Provides
    fun provideMoodEntryDao(db: ClockdinDatabase): MoodEntryDao = db.moodEntryDao()

    @Provides
    fun provideMoodColorDao(db: ClockdinDatabase): MoodColorDao = db.moodColorDao()
}