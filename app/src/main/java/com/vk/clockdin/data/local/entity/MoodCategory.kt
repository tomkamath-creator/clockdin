// Location: app/src/main/java/com/vk/clockdin/data/local/entity/MoodCategory.kt
package com.vk.clockdin.data.local.entity

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
