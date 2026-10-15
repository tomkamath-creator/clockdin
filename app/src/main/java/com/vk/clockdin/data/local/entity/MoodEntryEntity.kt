// Location: app/src/main/java/com/vk/clockdin/data/local/entity/MoodEntryEntity.kt
package com.vk.clockdin.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.vk.clockdin.domain.model.GradientStop
import com.vk.clockdin.domain.model.MoodEntry

@Entity(
    tableName = "mood_entries",
    indices = [
        Index(value = ["localDate"]),
        Index(value = ["createdAt"])
    ]
)
data class MoodEntryEntity(
    @PrimaryKey(autoGenerate = true)
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
    fun toDomain(): MoodEntry = MoodEntry(
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
        fun fromDomain(domain: MoodEntry): MoodEntryEntity = MoodEntryEntity(
            id = domain.id,
            createdAt = domain.createdAt,
            localDate = domain.localDate,
            gradientStops = domain.gradientStops,
            orbImagePath = domain.orbImagePath,
            note = domain.note,
            photoPath = domain.photoPath,
            songTitle = domain.songTitle,
            songArtist = domain.songArtist
        )
    }
}
