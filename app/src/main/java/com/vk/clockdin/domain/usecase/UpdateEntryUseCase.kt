// Location: app/src/main/java/com/vk/clockdin/domain/usecase/UpdateEntryUseCase.kt
package com.vk.clockdin.domain.usecase

import com.vk.clockdin.domain.model.MoodEntry
import com.vk.clockdin.domain.repository.MoodEntryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Updates an existing [MoodEntry] atomically in the repository.
 * Existing metadata (id, createdAt, localDate) is preserved, while
 * note, attached photos, gradient stops, and/or orbImagePath can be updated.
 */
class UpdateEntryUseCase @Inject constructor(
    private val moodEntryRepository: MoodEntryRepository
) {
    suspend operator fun invoke(entry: MoodEntry) = withContext(Dispatchers.IO) {
        moodEntryRepository.updateEntry(entry)
    }
}
