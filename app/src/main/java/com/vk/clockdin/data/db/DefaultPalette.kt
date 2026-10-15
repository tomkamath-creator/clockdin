// app/src/main/java/com/vk/clockdin/data/db/DefaultPalette.kt
package com.vk.clockdin.data.db

import com.vk.clockdin.domain.model.MoodCategory

/** Default mood color palette seeded on first DB creation. */
internal val DEFAULT_MOOD_COLORS = listOf(
    // ── Positive ────────────────────────────────────────────────────
    MoodColorEntity(name = "Joy",       hex = "#FFC83D", category = MoodCategory.POSITIVE, sortOrder = 0,  isDefault = true),
    MoodColorEntity(name = "Calm",      hex = "#2FD4A7", category = MoodCategory.POSITIVE, sortOrder = 1,  isDefault = true),
    MoodColorEntity(name = "Energetic", hex = "#FFB000", category = MoodCategory.POSITIVE, sortOrder = 2,  isDefault = true),
    MoodColorEntity(name = "Loving",    hex = "#FF8FA8", category = MoodCategory.POSITIVE, sortOrder = 3,  isDefault = true),
    // ── Neutral ─────────────────────────────────────────────────────
    MoodColorEntity(name = "Pensive",   hex = "#B8A9E8", category = MoodCategory.NEUTRAL,  sortOrder = 10, isDefault = true),
    MoodColorEntity(name = "Bored",     hex = "#708090", category = MoodCategory.NEUTRAL,  sortOrder = 11, isDefault = true),
    // ── Negative ────────────────────────────────────────────────────
    MoodColorEntity(name = "Anxious",   hex = "#9D4DFF", category = MoodCategory.NEGATIVE, sortOrder = 20, isDefault = true),
    MoodColorEntity(name = "Sad",       hex = "#2A4FBF", category = MoodCategory.NEGATIVE, sortOrder = 21, isDefault = true),
    MoodColorEntity(name = "Angry",     hex = "#DC143C", category = MoodCategory.NEGATIVE, sortOrder = 22, isDefault = true),
)