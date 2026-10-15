// app/src/main/java/com/vk/clockdin/domain/usecase/ComputeStreakUseCase.kt
package com.vk.clockdin.domain.usecase

import java.time.LocalDate
import javax.inject.Inject

/**
 * Computes the current streak: the number of consecutive calendar days
 * (ending today, or yesterday if today has no entry yet) that have >= 1 entry.
 *
 * @param entryDates Sorted list of ISO date strings (yyyy-MM-dd) that have entries.
 *                   May contain duplicates (multiple entries on same day).
 * @param today      The current local date (injected for testability).
 * @return Streak count >= 0.
 */
class ComputeStreakUseCase @Inject constructor() {

    operator fun invoke(
        entryDates: List<String>,
        today: LocalDate = LocalDate.now()
    ): Int {
        if (entryDates.isEmpty()) return 0

        // Deduplicate and parse
        val uniqueDates: Set<LocalDate> = entryDates
            .mapTo(HashSet()) { LocalDate.parse(it) }

        // Anchor: prefer today, fall back to yesterday
        val anchor = if (today in uniqueDates) today else today.minusDays(1)
        if (anchor !in uniqueDates) return 0

        var streak = 0
        var cursor = anchor
        while (cursor in uniqueDates) {
            streak++
            cursor = cursor.minusDays(1)
        }
        return streak
    }
}