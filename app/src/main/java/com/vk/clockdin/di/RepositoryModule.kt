// app/src/main/java/com/vk/clockdin/di/RepositoryModule.kt
package com.vk.clockdin.di

import com.vk.clockdin.data.datastore.SettingsRepositoryImpl
import com.vk.clockdin.data.repository.MoodColorRepositoryImpl
import com.vk.clockdin.data.repository.MoodEntryRepositoryImpl
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

    @Binds @Singleton
    abstract fun bindMoodEntryRepo(impl: MoodEntryRepositoryImpl): MoodEntryRepository

    @Binds @Singleton
    abstract fun bindMoodColorRepo(impl: MoodColorRepositoryImpl): MoodColorRepository

    @Binds @Singleton
    abstract fun bindSettingsRepo(impl: SettingsRepositoryImpl): SettingsRepository
}