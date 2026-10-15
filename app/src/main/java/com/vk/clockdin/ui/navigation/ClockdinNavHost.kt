// Location: app/src/main/java/com/vk/clockdin/ui/navigation/ClockdinNavHost.kt
package com.vk.clockdin.ui.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.vk.clockdin.ui.splash.IntroSplashScreen
import com.vk.clockdin.ui.theme.TextSecondary
import com.vk.clockdin.ui.theme.spaceBackground

@Composable
fun ClockdinNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        composable(
            route = Screen.Splash.route,
            exitTransition = { fadeOut(animationSpec = tween(600)) }
        ) {
            IntroSplashScreen(
                onSplashComplete = {
                    navController.navigate(Screen.Galaxy.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Galaxy.route,
            enterTransition = { fadeIn(animationSpec = tween(600)) }
        ) {
            com.vk.clockdin.ui.galaxy.GalaxyHomeScreen(
                onNavigateToPaint = { navController.navigate(Screen.Paint.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToView = { route -> navController.navigate(route) },
                onOpenDetail = { id -> navController.navigate(Screen.Detail.createRoute(id)) }
            )
        }

        composable(route = Screen.Paint.route) {
            com.vk.clockdin.ui.paint.PaintScreen(
                onBack = { navController.popBackStack() },
                onProceedToSave = { orbImagePath, dominantColors ->
                    // Handled in Phase 4 save flow; for now pop back or hold path
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.List.route) {
            NavigationPlaceholderScreen(
                title = "Journal List",
                onBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Calendar.route) {
            NavigationPlaceholderScreen(
                title = "Calendar",
                onBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Ribbon.route) {
            NavigationPlaceholderScreen(
                title = "Timeline Ribbon",
                onBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Settings.route) {
            NavigationPlaceholderScreen(
                title = "Settings",
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("entryId") { type = NavType.LongType })
        ) {
            com.vk.clockdin.ui.detail.EntryDetailScreen(
                onBack = { navController.popBackStack() },
                onOpenPhoto = { path ->
                    navController.navigate(Screen.PhotoViewer.createRoute(path))
                }
            )
        }

        composable(
            route = Screen.PhotoViewer.route,
            arguments = listOf(navArgument("photoPath") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedPath = backStackEntry.arguments?.getString("photoPath") ?: ""
            val decodedPath = try {
                java.net.URLDecoder.decode(encodedPath, java.nio.charset.StandardCharsets.UTF_8.name())
            } catch (_: Exception) {
                encodedPath
            }
            com.vk.clockdin.ui.detail.FullScreenPhotoViewer(
                photoPath = decodedPath,
                onClose = { navController.popBackStack() }
            )
        }
    }
}



@Composable
private fun NavigationPlaceholderScreen(
    title: String,
    onBack: () -> Unit
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .spaceBackground(),
        containerColor = androidx.compose.ui.graphics.Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                color = TextSecondary
            )
        }
    }
}
