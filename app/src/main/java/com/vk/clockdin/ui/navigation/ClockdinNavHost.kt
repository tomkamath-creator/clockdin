// Location: app/src/main/java/com/vk/clockdin/ui/navigation/ClockdinNavHost.kt
package com.vk.clockdin.ui.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.vk.clockdin.ui.splash.IntroSplashScreen

@Composable
fun ClockdinNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> (fullWidth * 0.12f).toInt() },
                animationSpec = tween(380, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(380, easing = FastOutSlowInEasing))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> -(fullWidth * 0.12f).toInt() },
                animationSpec = tween(320, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(320, easing = FastOutSlowInEasing))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> -(fullWidth * 0.12f).toInt() },
                animationSpec = tween(380, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(380, easing = FastOutSlowInEasing))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> (fullWidth * 0.12f).toInt() },
                animationSpec = tween(320, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(320, easing = FastOutSlowInEasing))
        }
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

        composable(
            route = Screen.Paint.route,
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { (it * 0.22f).toInt() },
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(400, easing = FastOutSlowInEasing))
            },
            exitTransition = {
                slideOutVertically(
                    targetOffsetY = { (it * 0.22f).toInt() },
                    animationSpec = tween(350, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(350, easing = FastOutSlowInEasing))
            }
        ) {
            com.vk.clockdin.ui.paint.PaintScreen(
                onBack = { navController.popBackStack() },
                onProceedToSave = { _, _ ->
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.List.route) {
            com.vk.clockdin.ui.list.JournalListScreen(
                onNavigateToPerspective = { perspective ->
                    if (perspective.route == Screen.Galaxy.route) {
                        navController.navigate(Screen.Galaxy.route) {
                            popUpTo(Screen.Galaxy.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(perspective.route) {
                            popUpTo(Screen.Galaxy.route)
                        }
                    }
                },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToPaint = { navController.navigate(Screen.Paint.route) },
                onEntryClick = { id -> navController.navigate(Screen.Detail.createRoute(id)) }
            )
        }

        composable(route = Screen.Calendar.route) {
            com.vk.clockdin.ui.calendar.CalendarScreen(
                onNavigateToPerspective = { perspective ->
                    if (perspective.route == Screen.Galaxy.route) {
                        navController.navigate(Screen.Galaxy.route) {
                            popUpTo(Screen.Galaxy.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(perspective.route) {
                            popUpTo(Screen.Galaxy.route)
                        }
                    }
                },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToPaint = { navController.navigate(Screen.Paint.route) },
                onEntryClick = { id -> navController.navigate(Screen.Detail.createRoute(id)) }
            )
        }

        composable(route = Screen.Ribbon.route) {
            com.vk.clockdin.ui.ribbon.RibbonScreen(
                onNavigateToPerspective = { perspective ->
                    if (perspective.route == Screen.Galaxy.route) {
                        navController.navigate(Screen.Galaxy.route) {
                            popUpTo(Screen.Galaxy.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(perspective.route) {
                            popUpTo(Screen.Galaxy.route)
                        }
                    }
                },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToPaint = { navController.navigate(Screen.Paint.route) },
                onEntryClick = { id -> navController.navigate(Screen.Detail.createRoute(id)) }
            )
        }

        composable(
            route = Screen.Settings.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(380, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(380, easing = FastOutSlowInEasing))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(320, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(320, easing = FastOutSlowInEasing))
            }
        ) {
            com.vk.clockdin.ui.settings.SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("entryId") { type = NavType.LongType }),
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { (it * 0.16f).toInt() },
                    animationSpec = tween(380, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(380, easing = FastOutSlowInEasing))
            },
            exitTransition = {
                slideOutVertically(
                    targetOffsetY = { (it * 0.16f).toInt() },
                    animationSpec = tween(320, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(320, easing = FastOutSlowInEasing))
            }
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
            arguments = listOf(navArgument("photoPath") { type = NavType.StringType }),
            enterTransition = {
                fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.92f, animationSpec = tween(300))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(250)) + scaleOut(targetScale = 0.92f, animationSpec = tween(250))
            }
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
