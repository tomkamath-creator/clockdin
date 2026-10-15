// Location: app/src/main/java/com/vk/clockdin/domain/usecase/CalculateWeeklyMoodStatsUseCase.kt
package com.vk.clockdin.domain.usecase

import com.vk.clockdin.domain.model.MoodEntry
import com.vk.clockdin.domain.model.MoodPercentageItem
import com.vk.clockdin.domain.model.WeeklyMoodStats
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * Calculates majority mood and percentage distribution of mood categories/colors
 * for the current rolling 7-day week (or recent entries).
 */
class CalculateWeeklyMoodStatsUseCase @Inject constructor() {

    operator fun invoke(
        entries: List<MoodEntry>,
        referenceDate: LocalDate = LocalDate.now()
    ): WeeklyMoodStats {
        if (entries.isEmpty()) {
            return WeeklyMoodStats()
        }

        // Filter entries within the last 7 days (including referenceDate)
        val weeklyEntries = entries.filter { entry ->
            try {
                val entryDate = LocalDate.parse(entry.localDate)
                val diffDays = ChronoUnit.DAYS.between(entryDate, referenceDate)
                diffDays in 0..6
            } catch (_: Exception) {
                false
            }
        }.ifEmpty {
            // Fallback: if no entries within the last 7 calendar days, evaluate the most recent up to 7 entries
            entries.take(7)
        }

        if (weeklyEntries.isEmpty()) {
            return WeeklyMoodStats()
        }

        // Aggregate mood counts across gradient stops
        val tally = mutableMapOf<String, Pair<String, Int>>() // label -> (hex, count)

        for (entry in weeklyEntries) {
            val stops = entry.gradientStops
            if (stops.isEmpty()) continue
            for (stop in stops) {
                val label = stop.label?.ifBlank { null } ?: "Mood"
                val hex = stop.hex
                val existing = tally[label]
                if (existing == null) {
                    tally[label] = Pair(hex, 1)
                } else {
                    tally[label] = Pair(existing.first, existing.second + 1)
                }
            }
        }

        val totalStops = tally.values.sumOf { it.second }
        if (totalStops == 0) {
            return WeeklyMoodStats(totalEntriesThisWeek = weeklyEntries.size)
        }

        // Compute integer percentages
        val breakdown = tally.entries
            .map { (label, data) ->
                val (hex, count) = data
                val pct = ((count.toFloat() / totalStops.toFloat()) * 100f).toInt().coerceIn(1, 100)
                MoodPercentageItem(
                    label = label,
                    hexColor = hex,
                    count = count,
                    percentage = pct
                )
            }
            .sortedByDescending { it.percentage }

        val majority = breakdown.firstOrNull()

        return WeeklyMoodStats(
            totalEntriesThisWeek = weeklyEntries.size,
            majorityMood = majority?.label,
            majorityPercentage = majority?.percentage ?: 0,
            majorityHexColor = majority?.hexColor,
            breakdown = breakdown
        )
    }
}
