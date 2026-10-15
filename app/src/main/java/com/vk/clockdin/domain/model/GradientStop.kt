// Location: app/src/main/java/com/vk/clockdin/domain/model/GradientStop.kt
package com.vk.clockdin.domain.model

import kotlinx.serialization.Serializable

/**
 * Domain model representing a single color stop along an orb's gradient radius or angle.
 *
 * @property hex Color in "#RRGGBB" or "#AARRGGBB" format.
 * @property position Normalized position along the gradient, in range [0.0..1.0].
 * @property label Optional mood emotion name (e.g., "Joy", "Calm").
 */
@Serializable
data class GradientStop(
    val hex: String,
    val position: Float,
    val label: String? = null
)
