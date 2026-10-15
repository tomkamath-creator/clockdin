// Location: app/src/main/java/com/vk/clockdin/data/local/converter/Converters.kt
package com.vk.clockdin.data.local.converter

import androidx.room.TypeConverter
import com.vk.clockdin.domain.model.GradientStop
import com.vk.clockdin.domain.model.MoodCategory
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Room TypeConverters for complex types using Kotlinx Serialization.
 */
class Converters {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @TypeConverter
    fun fromGradientStopsList(stops: List<GradientStop>?): String {
        if (stops.isNullOrEmpty()) return "[]"
        return json.encodeToString(stops)
    }

    @TypeConverter
    fun toGradientStopsList(jsonString: String?): List<GradientStop> {
        if (jsonString.isNullOrBlank() || jsonString == "[]") return emptyList()
        return try {
            json.decodeFromString<List<GradientStop>>(jsonString)
        } catch (_: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromMoodCategory(category: MoodCategory?): String {
        return category?.name ?: MoodCategory.NEUTRAL.name
    }

    @TypeConverter
    fun toMoodCategory(value: String?): MoodCategory {
        return try {
            if (value != null) MoodCategory.valueOf(value) else MoodCategory.NEUTRAL
        } catch (_: IllegalArgumentException) {
            MoodCategory.NEUTRAL
        }
    }
}
