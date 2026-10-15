// Location: app/src/main/java/com/vk/clockdin/data/local/entity/MoodColor.kt
package com.vk.clockdin.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Room entity representing an emotion color swatch in the palette.
 *
 * @property id Auto-generated primary key.
 * @property name Human-readable mood name (e.g. "Joy", "Pensive", "Sad").
 * @property hex Hexadecimal color representation (e.g. "#FFC83D").
 * @property category Grouping category (POSITIVE, NEUTRAL, NEGATIVE).
 * @property sortOrder Display ordering index within its category.
 * @property isDefault True if this color is part of the seeded default palette.
 */
@Serializable
@Entity(
    tableName = "mood_colors",
    indices = [
        Index(value = ["category", "sortOrder"])
    ]
)
data class MoodColor(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val hex: String,
    val category: MoodCategory,
    val sortOrder: Int,
    val isDefault: Boolean = false
)
