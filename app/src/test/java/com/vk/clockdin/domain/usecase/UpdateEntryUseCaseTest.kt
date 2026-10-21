// Location: app/src/test/java/com/vk/clockdin/domain/usecase/UpdateEntryUseCaseTest.kt
package com.vk.clockdin.domain.usecase

import com.vk.clockdin.domain.model.GradientStop
import com.vk.clockdin.domain.model.MoodEntry
import com.vk.clockdin.domain.repository.MoodEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class UpdateEntryUseCaseTest {

    private lateinit var fakeRepository: FakeMoodEntryRepository
    private lateinit var updateEntryUseCase: UpdateEntryUseCase

    @Before
    fun setup() {
        fakeRepository = FakeMoodEntryRepository()
        updateEntryUseCase = UpdateEntryUseCase(fakeRepository)
    }

    @Test
    fun updateEntry_updatesNoteAndPhotosWhilePreservingMetadata() = runBlocking {
        val original = MoodEntry(
            id = 42L,
            createdAt = 1700000000000L,
            localDate = "2026-10-15",
            gradientStops = listOf(
                GradientStop(hex = "#FFC83D", position = 0f, label = "Joy")
            ),
            orbImagePath = "/test/orbs/orb_42.png",
            note = "Original thought",
            photoPath = "/photos/p1.jpg"
        )
        fakeRepository.insertEntry(original)

        val updated = original.copy(
            note = "Updated reflection after reflection",
            photoPath = "/photos/p1.jpg||/photos/p2.jpg"
        )

        updateEntryUseCase(updated)

        val retrieved = fakeRepository.getEntryByIdDirect(42L)
        assertNotNull(retrieved)
        assertEquals(42L, retrieved!!.id)
        assertEquals("2026-10-15", retrieved.localDate)
        assertEquals(1700000000000L, retrieved.createdAt)
        assertEquals("Updated reflection after reflection", retrieved.note)
        assertEquals(2, retrieved.photoPaths.size)
        assertEquals("/photos/p1.jpg", retrieved.photoPaths[0])
        assertEquals("/photos/p2.jpg", retrieved.photoPaths[1])
    }

    private class FakeMoodEntryRepository : MoodEntryRepository {
        val entries = mutableListOf<MoodEntry>()

        override fun getAllEntries(): Flow<List<MoodEntry>> = flowOf(entries)
        override fun getAllEntriesAscending(): Flow<List<MoodEntry>> = flowOf(entries)
        override fun getEntryById(id: Long): Flow<MoodEntry?> = flowOf(entries.find { it.id == id })
        override suspend fun getEntryByIdDirect(id: Long): MoodEntry? = entries.find { it.id == id }
        override fun getEntriesBetweenDates(startDate: String, endDate: String): Flow<List<MoodEntry>> = flowOf(entries)
        override fun getEntriesByMonth(yearMonth: String): Flow<List<MoodEntry>> = flowOf(entries)
        override fun getEntriesForDate(date: String): Flow<List<MoodEntry>> = flowOf(entries.filter { it.localDate == date })
        override fun getAllDistinctDates(): Flow<List<String>> = flowOf(entries.map { it.localDate }.distinct())
        override fun getEntryCount(): Flow<Int> = flowOf(entries.size)
        override suspend fun insertEntry(entry: MoodEntry): Long {
            entries.add(entry)
            return entry.id
        }
        override suspend fun insertEntries(entries: List<MoodEntry>) {
            this.entries.addAll(entries)
        }
        override suspend fun updateEntry(entry: MoodEntry) {
            val idx = entries.indexOfFirst { it.id == entry.id }
            if (idx != -1) entries[idx] = entry
        }
        override suspend fun deleteEntry(entry: MoodEntry) {
            entries.removeAll { it.id == entry.id }
        }
        override suspend fun deleteEntryById(id: Long) {
            entries.removeAll { it.id == id }
        }
        override suspend fun deleteAllEntries() {
            entries.clear()
        }
    }
}
