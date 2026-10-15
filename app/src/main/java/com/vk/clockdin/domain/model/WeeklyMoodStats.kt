// Location: app/src/main/java/com/vk/clockdin/domain/model/WeeklyMoodStats.kt
package com.vk.clockdin.domain.model

data class MoodPercentageItem(
    val label: String,
    val hexColor: String,
    val count: Int,
    val percentage: Int
)

data class WeeklyMoodStats(
    val totalEntriesThisWeek: Int = 0,
    val majorityMood: String? = null,
    val majorityPercentage: Int = 0,
    val majorityHexColor: String? = null,
    val breakdown: List<MoodPercentageItem> = emptyList()
)
