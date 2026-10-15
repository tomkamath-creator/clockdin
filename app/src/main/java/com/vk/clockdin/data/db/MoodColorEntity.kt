// app/src/main/java/com/vk/clockdin/data/db/MoodColorEntity.kt
package com.vk.clockdin.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.vk.clockdin.domain.model.MoodCategory
import com.vk.clockdin.domain.model.MoodColor

@Entity(tableName = "mood_colors")
data class MoodColorEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val hex: String,
    val category: MoodCategory,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
    @ColumnInfo(name = "is_default") val isDefault: Boolean
) {
    fun toDomain() = MoodColor(
        id = id, name = name, hex = hex,
        category = category, sortOrder = sortOrder, isDefault = isDefault
    )

    companion object {
        fun fromDomain(d: MoodColor) = MoodColorEntity(
            id = d.id, name = d.name, hex = d.hex,
            category = d.category, sortOrder = d.sortOrder, isDefault = d.isDefault
        )
    }
}