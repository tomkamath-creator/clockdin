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
    data object Paint : Screen("paint")
    data object List : Screen("list")
    data object Calendar : Screen("calendar")
    data object Ribbon : Screen("ribbon")
    data object Settings : Screen("settings")

    data object Detail : Screen("detail/{entryId}") {
        fun createRoute(entryId: Long): String = "detail/$entryId"
    }

    data object PhotoViewer : Screen("photo_viewer/{photoPath}") {
        fun createRoute(photoPath: String): String {
            val encoded = URLEncoder.encode(photoPath, StandardCharsets.UTF_8.name())
            return "photo_viewer/$encoded"
        }
    }
}
