// app/src/main/java/com/vk/clockdin/domain/model/MoodColor.kt
package com.vk.clockdin.domain.model

/**
 * A user-facing mood color in the palette.
 */
data class MoodColor(
    val id: Long = 0L,
    val name: String,
    val hex: String,
    val category: MoodCategory,
    val sortOrder: Int,
    val isDefault: Boolean = false
)

enum class MoodCategory { POSITIVE, NEUTRAL, NEGATIVE }