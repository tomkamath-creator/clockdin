// Location: app/src/main/java/com/vk/clockdin/domain/usecase/CalculateStreakUseCase.kt
package com.vk.clockdin.domain.usecase

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject

/**
 * Calculates current streak count according to the rule:
 * Number of consecutive calendar days (ending today, or yesterday if today has no entry yet)
 * with at least 1 entry, computed from `localDate`.
 */
class CalculateStreakUseCase @Inject constructor() {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    operator fun invoke(
        dates: List<String>,
        today: LocalDate = LocalDate.now()
    ): Int {
        if (dates.isEmpty()) return 0

        val parsedDates = mutableSetOf<LocalDate>()
        for (dateStr in dates) {
            try {
                parsedDates.add(LocalDate.parse(dateStr, dateFormatter))
            } catch (_: DateTimeParseException) {
                // Ignore malformed strings
            }
        }

        if (parsedDates.isEmpty()) return 0

        val anchorDate = when {
            parsedDates.contains(today) -> today
            parsedDates.contains(today.minusDays(1)) -> today.minusDays(1)
            else -> return 0
        }

        var streak = 0
        var checkDate = anchorDate
        while (parsedDates.contains(checkDate)) {
            streak++
            checkDate = checkDate.minusDays(1)
        }

        return streak
    }
}
