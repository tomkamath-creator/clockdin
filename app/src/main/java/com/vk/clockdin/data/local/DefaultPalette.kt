// Location: app/src/main/java/com/vk/clockdin/data/local/DefaultPalette.kt
package com.vk.clockdin.data.local

import com.vk.clockdin.domain.model.MoodCategory
import com.vk.clockdin.domain.model.MoodColor

object DefaultPalette {
    val defaultColors = listOf(
        // Positive
        MoodColor(
            name = "Joy",
            hex = "#FFC83D",
            category = MoodCategory.POSITIVE,
            sortOrder = 0,
            isDefault = true
        ),
        MoodColor(
            name = "Calm",
            hex = "#2FD4A7",
            category = MoodCategory.POSITIVE,
            sortOrder = 1,
            isDefault = true
        ),
        MoodColor(
            name = "Energetic",
            hex = "#FFB000",
            category = MoodCategory.POSITIVE,
            sortOrder = 2,
            isDefault = true
        ),
        MoodColor(
            name = "Loving",
            hex = "#FF8FA8",
            category = MoodCategory.POSITIVE,
            sortOrder = 3,
            isDefault = true
        ),

        // Neutral
        MoodColor(
            name = "Pensive",
            hex = "#B8A9E8",
            category = MoodCategory.NEUTRAL,
            sortOrder = 4,
            isDefault = true
        ),
        MoodColor(
            name = "Bored",
            hex = "#708090",
            category = MoodCategory.NEUTRAL,
            sortOrder = 5,
            isDefault = true
        ),

        // Negative
        MoodColor(
            name = "Anxious",
            hex = "#9D4DFF",
            category = MoodCategory.NEGATIVE,
            sortOrder = 6,
            isDefault = true
        ),
        MoodColor(
            name = "Sad",
            hex = "#2A4FBF",
            category = MoodCategory.NEGATIVE,
            sortOrder = 7,
            isDefault = true
        ),
        MoodColor(
            name = "Angry",
            hex = "#DC143C",
            category = MoodCategory.NEGATIVE,
            sortOrder = 8,
            isDefault = true
        )
    )
}
