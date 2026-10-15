// app/src/main/java/com/vk/clockdin/domain/usecase/SaveEntryUseCase.kt
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
 * Persists a [MoodEntry] atomically: writes files first, then inserts the DB row.
 * On DB failure it cleans up any files written during this call.
 */
class SaveEntryUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repo: MoodEntryRepository
) {
    /**
     * @param entry  Domain model with [MoodEntry.orbImagePath] and optionally
     *               [MoodEntry.photoPath] already pointing to temp files.
     * @param orbBytes  Raw 512x512 PNG bytes for the orb.
     * @param photoBytes  Optional JPEG bytes for the attached photo.
     * @return The assigned DB id on success.
     * @throws Exception on any failure; temp files are cleaned up before re-throw.
     */
    suspend operator fun invoke(
        entry: MoodEntry,
        orbBytes: ByteArray,
        photoBytes: ByteArray? = null
    ): Long = withContext(Dispatchers.IO) {
        val orbDir = File(context.filesDir, "orbs").also { it.mkdirs() }
        val photoDir = File(context.filesDir, "photos").also { it.mkdirs() }

        val orbFile = File(orbDir, "orb_${System.currentTimeMillis()}.png")
        var photoFile: File? = null

        try {
            orbFile.writeBytes(orbBytes)
            if (photoBytes != null) {
                photoFile = File(photoDir, "photo_${System.currentTimeMillis()}.jpg")
                photoFile.writeBytes(photoBytes)
            }

            val finalEntry = entry.copy(
                orbImagePath = orbFile.absolutePath,
                photoPath = photoFile?.absolutePath
            )
            repo.insert(finalEntry)
        } catch (e: Exception) {
            // Clean up any files written during this invocation
            if (orbFile.exists()) orbFile.delete()
            photoFile?.let { if (it.exists()) it.delete() }
            throw e
        }
    }
}