// Location: app/src/main/java/com/vk/clockdin/domain/usecase/DeleteEntryUseCase.kt
package com.vk.clockdin.domain.usecase

import com.vk.clockdin.domain.model.MoodEntry
import com.vk.clockdin.domain.repository.MoodEntryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class DeleteEntryUseCase @Inject constructor(
    private val moodEntryRepository: MoodEntryRepository
) {
    suspend operator fun invoke(entry: MoodEntry) = withContext(Dispatchers.IO) {
        moodEntryRepository.deleteEntry(entry)
        // Clean up rendered orb and attached photo files
        entry.orbImagePath.let { path ->
            val orbFile = File(path)
            if (orbFile.exists()) orbFile.delete()
        }
        entry.photoPath?.let { path ->
            val photoFile = File(path)
            if (photoFile.exists()) photoFile.delete()
        }
    }
}
