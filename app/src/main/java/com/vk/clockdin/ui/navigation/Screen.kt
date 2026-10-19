// Location: app/src/main/java/com/vk/clockdin/ui/navigation/Screen.kt
package com.vk.clockdin.ui.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Type-safe navigation routes for Clockdin.
 */
sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Galaxy : Screen("galaxy")
    data object Paint : Screen("paint?editEntryId={editEntryId}") {
        fun createRoute(editEntryId: Long? = null): String =
            if (editEntryId != null) "paint?editEntryId=$editEntryId" else "paint"
    }
    data object List : Screen("list")
    data object Calendar : Screen("calendar")
    data object Ribbon : Screen("ribbon")
    data object Settings : Screen("settings")

    data object Detail : Screen("detail/{entryId}") {
        fun createRoute(entryId: Long): String = "detail/$entryId"
    }

    data object PhotoViewer : Screen("photo_viewer/{photoPaths}?index={initialIndex}") {
        fun createRoute(photoPaths: kotlin.collections.List<String>, initialIndex: Int = 0): String {
            val joined = photoPaths.joinToString("|||")
            val encoded = URLEncoder.encode(joined, StandardCharsets.UTF_8.name())
            return "photo_viewer/$encoded?index=$initialIndex"
        }

        fun createRoute(photoPath: String): String {
            return createRoute(kotlin.collections.listOf(photoPath), 0)
        }
    }
}
