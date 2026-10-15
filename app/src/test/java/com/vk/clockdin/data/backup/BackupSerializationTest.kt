// Location: app/src/test/java/com/vk/clockdin/data/backup/BackupSerializationTest.kt
package com.vk.clockdin.data.backup

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class BackupSerializationTest {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    @Test
    fun cosmicBackup_serializesAndDeserializesCorrectly() {
        val backup = CosmicBackup(
            version = 1,
            exportTimestamp = 1700000000000L,
            entries = listOf(
                BackupEntry(
                    createdAt = 1700000000000L,
                    localDate = "2026-10-15",
                    gradientStops = listOf(
                        BackupGradientStop(hex = "#FFC83D", position = 0f, label = "Joy"),
                        BackupGradientStop(hex = "#2FD4A7", position = 1f, label = "Calm")
                    ),
                    note = "A serene day in space",
                    orbBase64 = "dGVzdF9iYXNlNjQ="
                )
            )
        )

        val jsonString = json.encodeToString(backup)
        val deserialized = json.decodeFromString<CosmicBackup>(jsonString)

        assertEquals(1, deserialized.version)
        assertEquals(1, deserialized.entries.size)
        val entry = deserialized.entries[0]
        assertEquals("2026-10-15", entry.localDate)
        assertEquals("A serene day in space", entry.note)
        assertEquals(2, entry.gradientStops.size)
        assertEquals("#FFC83D", entry.gradientStops[0].hex)
        assertEquals("Joy", entry.gradientStops[0].label)
        assertEquals("dGVzdF9iYXNlNjQ=", entry.orbBase64)
    }
}
