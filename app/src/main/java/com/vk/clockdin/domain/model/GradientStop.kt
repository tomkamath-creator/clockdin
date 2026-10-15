// app/src/main/java/com/vk/clockdin/domain/model/GradientStop.kt
package com.vk.clockdin.domain.model

import kotlinx.serialization.Serializable

/**
 * One color stop in an orb's gradient description.
 * @param hex  ARGB or RGB hex string, e.g. "#FF2FD4A7"
 * @param position Normalized 0..1 position along the radius
 * @param label Optional user-visible mood label at this stop
 */
@Serializable
data class GradientStop(
    val hex: String,
    val position: Float,
    val label: String? = null
)