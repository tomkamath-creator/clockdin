// Location: app/src/main/java/com/vk/clockdin/domain/usecase/SaveEntryUseCase.kt
package com.vk.clockdin.domain.usecase

import android.content.Context
import com.vk.clockdin.domain.model.MoodEntry
import com.vk.clockdin.domain.repository.MoodEntryRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/**
 * Persists a [MoodEntry] atomically: writes PNG orb and optional photo files first,
 * then inserts the Room row. On any failure, files written during this call are safely cleaned up.
 */
class SaveEntryUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val moodEntryRepository: MoodEntryRepository
) {
    /**
     * @param entry Domain model with metadata and gradient stops.
     * @param orbBytes Rendered 512x512 circular transparent PNG bytes.
     * @param photoBytes Optional downscaled JPEG photo bytes.
     * @return Assigned database row ID.
     */
    suspend operator fun invoke(
        entry: MoodEntry,
        orbBytes: ByteArray,
        photoBytes: ByteArray? = null
    ): Long = withContext(Dispatchers.IO) {
        val orbDir = File(context.filesDir, "orbs").also { it.mkdirs() }
        val photoDir = File(context.filesDir, "photos").also { it.mkdirs() }

        val timestamp = System.currentTimeMillis()
        val orbFile = File(orbDir, "orb_$timestamp.png")
        var photoFile: File? = null

        try {
            orbFile.writeBytes(orbBytes)

            if (photoBytes != null) {
                photoFile = File(photoDir, "photo_$timestamp.jpg")
                photoFile.writeBytes(photoBytes)
            }

            val finalEntry = entry.copy(
                orbImagePath = orbFile.absolutePath,
                photoPath = photoFile?.absolutePath
            )
            moodEntryRepository.insertEntry(finalEntry)
        } catch (e: Exception) {
            // Transactional rollback of newly written files on failure
            if (orbFile.exists()) orbFile.delete()
            photoFile?.let { if (it.exists()) it.delete() }
            throw e
        }
    }

    /**
     * Directly persists a [MoodEntry] whose orb and optional photo files are already saved on disk.
     */
    suspend operator fun invoke(entry: MoodEntry): Long = withContext(Dispatchers.IO) {
        moodEntryRepository.insertEntry(entry)
    }
}
