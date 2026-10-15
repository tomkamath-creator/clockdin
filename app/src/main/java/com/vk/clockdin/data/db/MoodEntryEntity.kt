// app/src/main/java/com/vk/clockdin/data/db/MoodEntryEntity.kt
package com.vk.clockdin.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.vk.clockdin.domain.model.GradientStop
import com.vk.clockdin.domain.model.MoodEntry

@Entity(
    tableName = "mood_entries",
    indices = [Index(value = ["local_date"])]
)
data class MoodEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "local_date") val localDate: String,
    @ColumnInfo(name = "gradient_stops") val gradientStops: List<GradientStop>,
    @ColumnInfo(name = "orb_image_path") val orbImagePath: String,
    val note: String?,
    @ColumnInfo(name = "photo_path") val photoPath: String?,
    @ColumnInfo(name = "song_title") val songTitle: String?,
    @ColumnInfo(name = "song_artist") val songArtist: String?
) {
    fun toDomain() = MoodEntry(
        id = id,
        createdAt = createdAt,
        localDate = localDate,
        gradientStops = gradientStops,
        orbImagePath = orbImagePath,
        note = note,
        photoPath = photoPath,
        songTitle = songTitle,
        songArtist = songArtist
    )

    companion object {
        fun fromDomain(d: MoodEntry) = MoodEntryEntity(
            id = d.id,
            createdAt = d.createdAt,
            localDate = d.localDate,
            gradientStops = d.gradientStops,
            orbImagePath = d.orbImagePath,
            note = d.note,
            photoPath = d.photoPath,
            songTitle = d.songTitle,
            songArtist = d.songArtist
        )
    }
}