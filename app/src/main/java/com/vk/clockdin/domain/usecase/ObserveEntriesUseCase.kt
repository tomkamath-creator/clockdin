// Location: app/src/main/java/com/vk/clockdin/domain/usecase/ObserveEntriesUseCase.kt
package com.vk.clockdin.domain.usecase

import com.vk.clockdin.domain.model.MoodEntry
import com.vk.clockdin.domain.repository.MoodEntryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveEntriesUseCase @Inject constructor(
    private val moodEntryRepository: MoodEntryRepository
) {
    operator fun invoke(): Flow<List<MoodEntry>> =
        moodEntryRepository.getAllEntries()
}
