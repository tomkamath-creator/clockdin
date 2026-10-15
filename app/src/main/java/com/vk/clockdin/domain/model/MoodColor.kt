// Location: app/src/main/java/com/vk/clockdin/domain/model/MoodColor.kt
package com.vk.clockdin.domain.model

import kotlinx.serialization.Serializable

/**
 * Mood categories classifying default and custom palette colors.
 */
@Serializable
enum class MoodCategory {
    POSITIVE,
    NEUTRAL,
    NEGATIVE
}

/**
 * Domain model representing an emotion color swatch in the palette.
 */
@Serializable
data class MoodColor(
    val id: Long = 0L,
    val name: String,
    val hex: String,
    val category: MoodCategory,
    val sortOrder: Int,
    val isDefault: Boolean = false
)
