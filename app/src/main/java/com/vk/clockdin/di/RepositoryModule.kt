// Location: app/src/main/java/com/vk/clockdin/di/RepositoryModule.kt
package com.vk.clockdin.di

import com.vk.clockdin.data.repository.DataStoreSettingsRepository
import com.vk.clockdin.data.repository.RoomMoodColorRepository
import com.vk.clockdin.data.repository.RoomMoodEntryRepository
import com.vk.clockdin.domain.repository.MoodColorRepository
import com.vk.clockdin.domain.repository.MoodEntryRepository
import com.vk.clockdin.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMoodEntryRepository(
        impl: RoomMoodEntryRepository
    ): MoodEntryRepository

    @Binds
    @Singleton
    abstract fun bindMoodColorRepository(
        impl: RoomMoodColorRepository
    ): MoodColorRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: DataStoreSettingsRepository
    ): SettingsRepository
}
