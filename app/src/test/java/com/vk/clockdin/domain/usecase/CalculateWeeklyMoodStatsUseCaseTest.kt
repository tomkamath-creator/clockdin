// Location: app/src/test/java/com/vk/clockdin/domain/usecase/CalculateWeeklyMoodStatsUseCaseTest.kt
package com.vk.clockdin.domain.usecase

import com.vk.clockdin.domain.model.GradientStop
import com.vk.clockdin.domain.model.MoodEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CalculateWeeklyMoodStatsUseCaseTest {

    private val useCase = CalculateWeeklyMoodStatsUseCase()

    @Test
    fun emptyEntries_returnsEmptyStats() {
        val result = useCase(emptyList())
        assertEquals(0, result.totalEntriesThisWeek)
        assertEquals(null, result.majorityMood)
        assertEquals(0, result.majorityPercentage)
        assertTrue(result.breakdown.isEmpty())
    }

    @Test
    fun singleEntry_calculatesCorrectMajorityMood() {
        val entries = listOf(
            MoodEntry(
                id = 1L,
                createdAt = 1700000000000L,
                localDate = "2026-10-15",
                gradientStops = listOf(
                    GradientStop(hex = "#FFC83D", position = 0f, label = "Joy")
                ),
                orbImagePath = "/path/orb1.png"
            )
        )

        val result = useCase(entries, referenceDate = LocalDate.parse("2026-10-15"))
        assertEquals(1, result.totalEntriesThisWeek)
        assertEquals("Joy", result.majorityMood)
        assertEquals(100, result.majorityPercentage)
        assertEquals("#FFC83D", result.majorityHexColor)
        assertEquals(1, result.breakdown.size)
    }

    @Test
    fun multipleEntries_calculatesMajorityPercentagesCorrectly() {
        val entries = listOf(
            MoodEntry(
                id = 1L,
                createdAt = 1700000000000L,
                localDate = "2026-10-15",
                gradientStops = listOf(
                    GradientStop(hex = "#FFC83D", position = 0f, label = "Joy")
                ),
                orbImagePath = "/path/orb1.png"
            ),
            MoodEntry(
                id = 2L,
                createdAt = 1700000000000L,
                localDate = "2026-10-14",
                gradientStops = listOf(
                    GradientStop(hex = "#FFC83D", position = 0f, label = "Joy")
                ),
                orbImagePath = "/path/orb2.png"
            ),
            MoodEntry(
                id = 3L,
                createdAt = 1700000000000L,
                localDate = "2026-10-13",
                gradientStops = listOf(
                    GradientStop(hex = "#2FD4A7", position = 0f, label = "Calm")
                ),
                orbImagePath = "/path/orb3.png"
            )
        )

        val result = useCase(entries, referenceDate = LocalDate.parse("2026-10-15"))
        assertEquals(3, result.totalEntriesThisWeek)
        assertEquals("Joy", result.majorityMood)
        // Joy is 2/3 = 66%
        assertEquals(66, result.majorityPercentage)
        assertEquals(2, result.breakdown.size)
        assertEquals("Joy", result.breakdown[0].label)
        assertEquals("Calm", result.breakdown[1].label)
        assertEquals(33, result.breakdown[1].percentage)
    }

    @Test
    fun multiplePhotosInMoodEntry_parsesCorrectly() {
        val entry = MoodEntry(
            id = 1L,
            createdAt = 1700000000000L,
            localDate = "2026-10-15",
            gradientStops = emptyList(),
            orbImagePath = "/path/orb.png",
            photoPath = "/path/photo1.jpg||/path/photo2.jpg||/path/photo3.jpg"
        )

        assertEquals(3, entry.photoPaths.size)
        assertEquals("/path/photo1.jpg", entry.photoPaths[0])
        assertEquals("/path/photo2.jpg", entry.photoPaths[1])
        assertEquals("/path/photo3.jpg", entry.photoPaths[2])
    }

    @Test
    fun multipleEntriesSameDay_aggregatesCorrectly() {
        val entries = listOf(
            MoodEntry(
                id = 1L,
                createdAt = 1700000000000L,
                localDate = "2026-10-15",
                gradientStops = listOf(
                    GradientStop(hex = "#FFC83D", position = 0f, label = "Joy")
                ),
                orbImagePath = "/path/orb1.png"
            ),
            MoodEntry(
                id = 2L,
                createdAt = 1700003600000L,
                localDate = "2026-10-15",
                gradientStops = listOf(
                    GradientStop(hex = "#FFC83D", position = 0f, label = "Joy")
                ),
                orbImagePath = "/path/orb2.png"
            ),
            MoodEntry(
                id = 3L,
                createdAt = 1700007200000L,
                localDate = "2026-10-15",
                gradientStops = listOf(
                    GradientStop(hex = "#2FD4A7", position = 0f, label = "Calm")
                ),
                orbImagePath = "/path/orb3.png"
            )
        )

        val result = useCase(entries, referenceDate = LocalDate.parse("2026-10-15"))
        assertEquals(3, result.totalEntriesThisWeek)
        assertEquals("Joy", result.majorityMood)
        assertEquals(66, result.majorityPercentage)
        assertEquals(2, result.breakdown.size)
    }
}
