// app/src/main/java/com/vk/clockdin/domain/model/AppSettings.kt
package com.vk.clockdin.domain.model

data class AppSettings(
    val hapticsEnabled: Boolean = true,
    val reminderEnabled: Boolean = false,
    /** HH:MM 24-h local time for the daily reminder, e.g. "20:00" */
    val reminderTime: String = "20:00",
    /** Epoch millis of last backup, 0 = never */
    val lastBackupTimestamp: Long = 0L
)