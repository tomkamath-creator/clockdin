// Location: app/src/main/java/com/vk/clockdin/domain/model/MoodEntry.kt
package com.vk.clockdin.domain.model

import kotlinx.serialization.Serializable

/**
 * Domain model representing a saved daily mood orb and journal entry.
 *
 * @property id Unique entry identifier.
 * @property createdAt Epoch milliseconds when the entry was created.
 * @property localDate ISO-8601 formatted date string ("yyyy-MM-dd") for calendar and streak queries.
 * @property gradientStops Dominant mood colors with normalized stop positions.
 * @property orbImagePath Absolute or relative file path to the rendered 512x512 transparent PNG in filesDir/orbs/.
 * @property note Optional reflective note text.
 * @property photoPath Optional downscaled JPEG photo in filesDir/photos/.
 * @property songTitle Optional song title of the day.
 * @property songArtist Optional artist name for the song of the day.
 */
@Serializable
data class MoodEntry(
    val id: Long = 0L,
    val createdAt: Long,
    val localDate: String,
    val gradientStops: List<GradientStop>,
    val orbImagePath: String,
    val note: String? = null,
    val photoPath: String? = null,
    val songTitle: String? = null,
    val songArtist: String? = null
) {
    val photoPaths: List<String>
        get() = if (photoPath.isNullOrBlank()) {
            emptyList()
        } else {
            photoPath.split("||").map { it.trim() }.filter { it.isNotEmpty() }
        }

    fun withPhotos(photos: List<String>): MoodEntry = copy(
        photoPath = if (photos.isEmpty()) null else photos.joinToString("||")
    )
}

