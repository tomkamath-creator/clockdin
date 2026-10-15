// Location: app/src/main/java/com/vk/clockdin/ui/theme/Color.kt
package com.vk.clockdin.ui.theme

import androidx.compose.ui.graphics.Color

// Background deep space gradient
val CosmicAbyss = Color(0xFF090A0F)
val PureBlack = Color(0xFF000000)

// Glassmorphic Chrome (strictly neutral white with low alpha)
val GlassSurfaceUltraLow = Color(0x0AFFFFFF) // ~4% alpha
val GlassSurfaceLow = Color(0x14FFFFFF)      // ~8% alpha
val GlassSurfaceMedium = Color(0x1AFFFFFF)   // ~10% alpha
val GlassBorderHairline = Color(0x1FFFFFFF)  // ~12% alpha
val GlassHighlightSoft = Color(0x28FFFFFF)   // ~16% inner highlight

// Neutral Text Hierarchy
val TextPrimary = Color(0xFFF2F3F7)
val TextSecondary = Color(0x99F2F3F7) // 60% alpha
val TextTertiary = Color(0x59F2F3F7)  // 35% alpha
val TextDisabled = Color(0x33F2F3F7)  // 20% alpha

// Default Mood Palette Colors (The ONLY saturated colors in the app)
// Positive
val MoodJoy = Color(0xFFFFC83D)       // Warm gold
val MoodCalm = Color(0xFF2FD4A7)      // Emerald teal
val MoodEnergetic = Color(0xFFFFB000) // Electric amber
val MoodLoving = Color(0xFFFF8FA8)    // Soft rose

// Neutral
val MoodPensive = Color(0xFFB8A9E8)   // Lavender mist
val MoodBored = Color(0xFF708090)     // Slate gray

// Negative
val MoodAnxious = Color(0xFF9D4DFF)   // Neon violet
val MoodSad = Color(0xFF2A4FBF)       // Deep cobalt
val MoodAngry = Color(0xFFDC143C)     // Crimson ember
