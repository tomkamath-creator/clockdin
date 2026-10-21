// Location: app/src/main/java/com/vk/clockdin/data/backup/BackupManager.kt
package com.vk.clockdin.data.backup

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.vk.clockdin.domain.model.GradientStop
import com.vk.clockdin.domain.model.MoodEntry
import com.vk.clockdin.domain.repository.MoodEntryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class CosmicBackup(
    val version: Int = 1,
    val exportTimestamp: Long = System.currentTimeMillis(),
    val entries: List<BackupEntry> = emptyList()
)

@Serializable
data class BackupEntry(
    val createdAt: Long,
    val localDate: String,
    val gradientStops: List<BackupGradientStop>,
    val note: String? = null,
    val orbBase64: String? = null,
    val photosBase64: List<String> = emptyList()
)

@Serializable
data class BackupGradientStop(
    val hex: String,
    val position: Float,
    val label: String? = null
)

@Singleton
class BackupManager @Inject constructor(
    private val moodEntryRepository: MoodEntryRepository
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    suspend fun exportBackup(context: Context, destinationUri: Uri): Int = withContext(Dispatchers.IO) {
        val entries = moodEntryRepository.getAllEntries().firstOrNull() ?: emptyList()
        val backupEntries = entries.map { entry ->
            val orbBase64 = if (entry.orbImagePath.isNotBlank()) {
                val file = File(entry.orbImagePath)
                if (file.exists()) {
                    val bytes = file.readBytes()
                    Base64.encodeToString(bytes, Base64.NO_WRAP)
                } else null
            } else null

            val photosBase64 = entry.photoPath?.split("||")?.mapNotNull { path ->
                val f = File(path.trim())
                if (f.exists()) {
                    val bytes = f.readBytes()
                    Base64.encodeToString(bytes, Base64.NO_WRAP)
                } else null
            } ?: emptyList()

            BackupEntry(
                createdAt = entry.createdAt,
                localDate = entry.localDate,
                gradientStops = entry.gradientStops.map {
                    BackupGradientStop(it.hex, it.position, it.label)
                },
                note = entry.note,
                orbBase64 = orbBase64,
                photosBase64 = photosBase64
            )
        }

        val backup = CosmicBackup(
            version = 1,
            exportTimestamp = System.currentTimeMillis(),
            entries = backupEntries
        )

        val jsonString = json.encodeToString(backup)

        context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
            outputStream.write(jsonString.toByteArray(Charsets.UTF_8))
        }

        entries.size
    }

    suspend fun importBackup(context: Context, sourceUri: Uri): Int = withContext(Dispatchers.IO) {
        val jsonString = context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
            inputStream.bufferedReader(Charsets.UTF_8).readText()
        } ?: return@withContext 0

        val backup = json.decodeFromString<CosmicBackup>(jsonString)
        val orbsDir = File(context.filesDir, "orbs").also { it.mkdirs() }

        val moodEntries = backup.entries.mapIndexed { index, bEntry ->
            val orbPath = if (!bEntry.orbBase64.isNullOrBlank()) {
                val orbFile = File(orbsDir, "restored_orb_${bEntry.createdAt}_$index.png")
                try {
                    val bytes = Base64.decode(bEntry.orbBase64, Base64.DEFAULT)
                    FileOutputStream(orbFile).use { it.write(bytes) }
                    orbFile.absolutePath
                } catch (_: Exception) {
                    ""
                }
            } else {
                ""
            }

            val photoPaths = if (bEntry.photosBase64.isNotEmpty()) {
                val photosDir = File(context.filesDir, "photos").also { it.mkdirs() }
                val restored = bEntry.photosBase64.mapIndexedNotNull { pIndex, pBase64 ->
                    try {
                        val pFile = File(photosDir, "restored_photo_${bEntry.createdAt}_${index}_$pIndex.jpg")
                        val bytes = Base64.decode(pBase64, Base64.DEFAULT)
                        FileOutputStream(pFile).use { it.write(bytes) }
                        pFile.absolutePath
                    } catch (_: Exception) {
                        null
                    }
                }
                if (restored.isEmpty()) null else restored.joinToString("||")
            } else {
                null
            }

            MoodEntry(
                createdAt = bEntry.createdAt,
                localDate = bEntry.localDate,
                gradientStops = bEntry.gradientStops.map {
                    GradientStop(it.hex, it.position, it.label)
                },
                orbImagePath = orbPath,
                note = bEntry.note,
                photoPath = photoPaths,
                songTitle = null,
                songArtist = null
            )
        }

        moodEntryRepository.insertEntries(moodEntries)
        moodEntries.size
    }

    suspend fun clearAllData(context: Context) = withContext(Dispatchers.IO) {
        moodEntryRepository.deleteAllEntries()

        // Clear local files
        val orbsDir = File(context.filesDir, "orbs")
        if (orbsDir.exists()) {
            orbsDir.deleteRecursively()
            orbsDir.mkdirs()
        }

        val photosDir = File(context.filesDir, "photos")
        if (photosDir.exists()) {
            photosDir.deleteRecursively()
            photosDir.mkdirs()
        }
    }
}
