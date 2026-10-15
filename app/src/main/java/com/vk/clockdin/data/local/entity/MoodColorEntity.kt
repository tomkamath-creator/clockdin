// Location: app/src/main/java/com/vk/clockdin/data/local/entity/MoodColorEntity.kt
package com.vk.clockdin.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.vk.clockdin.domain.model.MoodCategory
import com.vk.clockdin.domain.model.MoodColor

@Entity(
    tableName = "mood_colors",
    indices = [
        Index(value = ["category", "sortOrder"])
    ]
)
data class MoodColorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val hex: String,
    val category: MoodCategory,
    val sortOrder: Int,
    val isDefault: Boolean = false
) {
    fun toDomain(): MoodColor = MoodColor(
        id = id,
        name = name,
        hex = hex,
        category = category,
        sortOrder = sortOrder,
        isDefault = isDefault
    )

    companion object {
        fun fromDomain(domain: MoodColor): MoodColorEntity = MoodColorEntity(
            id = domain.id,
            name = domain.name,
            hex = domain.hex,
            category = domain.category,
            sortOrder = domain.sortOrder,
            isDefault = domain.isDefault
        )
    }
}
