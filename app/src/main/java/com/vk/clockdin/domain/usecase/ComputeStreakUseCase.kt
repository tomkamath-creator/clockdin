// Location: app/src/main/java/com/vk/clockdin/domain/usecase/ComputeStreakUseCase.kt
package com.vk.clockdin.domain.usecase

import java.time.LocalDate
import javax.inject.Inject

/**
 * Computes consecutive calendar days streak ending today, or yesterday if today has no entry yet.
 */
class ComputeStreakUseCase @Inject constructor(
    private val calculateStreakUseCase: CalculateStreakUseCase
) {
    operator fun invoke(
        entryDates: List<String>,
        today: LocalDate = LocalDate.now()
    ): Int = calculateStreakUseCase(dates = entryDates, today = today)
}
