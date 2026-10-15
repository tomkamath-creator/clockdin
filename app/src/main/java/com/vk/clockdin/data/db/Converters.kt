// app/src/main/java/com/vk/clockdin/data/db/Converters.kt
package com.vk.clockdin.data.db

import androidx.room.TypeConverter
import com.vk.clockdin.domain.model.GradientStop
import com.vk.clockdin.domain.model.MoodCategory
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

class Converters {

    @TypeConverter
    fun gradientStopsToJson(stops: List<GradientStop>): String =
        json.encodeToString(stops)

    @TypeConverter
    fun jsonToGradientStops(raw: String): List<GradientStop> =
        runCatching { json.decodeFromString<List<GradientStop>>(raw) }
            .getOrDefault(emptyList())

    @TypeConverter
    fun moodCategoryToString(cat: MoodCategory): String = cat.name

    @TypeConverter
    fun stringToMoodCategory(raw: String): MoodCategory =
        MoodCategory.entries.firstOrNull { it.name == raw } ?: MoodCategory.NEUTRAL
}