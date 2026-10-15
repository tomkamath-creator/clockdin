// app/src/main/java/com/vk/clockdin/domain/model/MoodEntry.kt
package com.vk.clockdin.domain.model

/**
 * Domain model for a single mood entry. Mirrors the Room entity but is
 * decoupled from persistence concerns.
 */
data class MoodEntry(
    val id: Long = 0L,
    /** Epoch millis (UTC) when the entry was created. */
    val createdAt: Long,
    /** ISO yyyy-MM-dd in the device local timezone. Used for streak/calendar queries. */
    val localDate: String,
    /** Gradient color stops extracted from the painted orb. */
    val gradientStops: List<GradientStop>,
    /** Absolute path to the rendered 512x512 orb PNG inside filesDir/orbs/. */
    val orbImagePath: String,
    val note: String? = null,
    /** Absolute path to the attached photo inside filesDir/photos/ (may be null). */
    val photoPath: String? = null,
    val songTitle: String? = null,
    val songArtist: String? = null
)