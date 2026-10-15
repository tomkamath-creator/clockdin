// Location: app/src/main/java/com/vk/clockdin/data/local/entity/MoodEntry.kt
package com.vk.clockdin.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Room entity representing a saved daily mood orb and journal entry.
 *
 * @property id Auto-generated primary key.
 * @property createdAt Epoch milliseconds when the entry was created.
 * @property localDate ISO-8601 formatted date string ("yyyy-MM-dd") for calendar and streak queries. Indexed.
 * @property gradientStops Dominant mood colors with normalized stop positions (saved as JSON).
 * @property orbImagePath Absolute or app-relative file path to the rendered 512x512 transparent PNG in filesDir/orbs/.
 * @property note Optional reflective note text.
 * @property photoPath Optional downscaled JPEG photo in filesDir/photos/.
 * @property songTitle Optional song title of the day.
 * @property songArtist Optional artist name for the song of the day.
 */
@Serializable
@Entity(
    tableName = "mood_entries",
    indices = [
        Index(value = ["localDate"]),
        Index(value = ["createdAt"])
    ]
)
data class MoodEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val createdAt: Long,
    val localDate: String,
    val gradientStops: List<GradientStop>,
    val orbImagePath: String,
    val note: String? = null,
    val photoPath: String? = null,
    val songTitle: String? = null,
    val songArtist: String? = null
)
