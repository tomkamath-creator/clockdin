// Location: app/src/main/java/com/vk/clockdin/domain/model/UserSettings.kt
package com.vk.clockdin.domain.model

/**
 * Immutable user configuration stored in DataStore.
 *
 * @property hapticsEnabled Whether canvas painting and save interactions trigger haptic feedback.
 * @property reminderEnabled Whether daily WorkManager reminder notifications are scheduled.
 * @property reminderHour 24-hour format reminder hour (0..23). Default 21 (9 PM).
 * @property reminderMinute Reminder minute (0..59). Default 0.
 * @property lastBackupTimestamp Epoch millis of the most recent local or cloud backup.
 */
data class UserSettings(
    val hapticsEnabled: Boolean = true,
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 21,
    val reminderMinute: Int = 0,
    val lastBackupTimestamp: Long = 0L
)
