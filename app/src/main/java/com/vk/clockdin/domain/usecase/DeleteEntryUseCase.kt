// app/src/main/java/com/vk/clockdin/domain/usecase/DeleteEntryUseCase.kt
package com.vk.clockdin.domain.usecase

import com.vk.clockdin.domain.model.MoodEntry
import com.vk.clockdin.domain.repository.MoodEntryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class DeleteEntryUseCase @Inject constructor(
    private val repo: MoodEntryRepository
) {
    suspend operator fun invoke(entry: MoodEntry) = withContext(Dispatchers.IO) {
        repo.delete(entry)
        // Best-effort file cleanup
        entry.orbImagePath.let { File(it).delete() }
        entry.photoPath?.let { File(it).delete() }
    }
}