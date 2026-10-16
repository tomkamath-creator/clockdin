// Location: app/src/main/java/com/vk/clockdin/ui/splash/IntroSplashScreen.kt
package com.vk.clockdin.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vk.clockdin.R
import com.vk.clockdin.ui.theme.WordmarkStyle
import com.vk.clockdin.ui.theme.spaceBackground
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Animated intro splash screen:
 * Displays cosmic space clouds (nebulae) in the background, frosted-white "clockdin" wordmark,
 * and an ultra-vivid, glowing colorful "by vk" subtitle.
 */
@Composable
fun IntroSplashScreen(
    onSplashComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alphaAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(0.92f) }

    // Ambient floating nebula motion and pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "splash_nebula")
    val cloudPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloud_pulse"
    )
    val cloudDriftX by infiniteTransition.animateFloat(
        initialValue = -35f,
        targetValue = 35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloud_drift_x"
    )
    val cloudDriftY by infiniteTransition.animateFloat(
        initialValue = -25f,
        targetValue = 25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloud_drift_y"
    )

    LaunchedEffect(Unit) {
        launch {
            alphaAnim.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        launch {
            scaleAnim.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }

        // Hold ~1.2s before cross-fading to Galaxy Home
        delay(1200L)
        onSplashComplete()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .spaceBackground(),
        contentAlignment = Alignment.Center
    ) {
        // Space clouds (cosmic nebulae) drifting and floating in background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f

            // Nebula 1: Deep cosmic violet cloud
            val center1 = Offset(cx - 80f + cloudDriftX, cy - 60f + cloudDriftY)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x357B1FA2),
                        Color(0x184A148C),
                        Color.Transparent
                    ),
                    center = center1,
                    radius = 320f * cloudPulse
                ),
                radius = 320f * cloudPulse,
                center = center1
            )

            // Nebula 2: Ethereal cyan/teal space cloud
            val center2 = Offset(cx + 90f - cloudDriftX * 0.8f, cy + 50f - cloudDriftY * 0.9f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x3000897B),
                        Color(0x12004D40),
                        Color.Transparent
                    ),
                    center = center2,
                    radius = 280f * cloudPulse
                ),
                radius = 280f * cloudPulse,
                center = center2
            )

            // Nebula 3: Soft warm gold cosmic dust center
            val center3 = Offset(cx + cloudDriftY * 0.5f, cy + 10f + cloudDriftX * 0.5f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x22FFA000),
                        Color.Transparent
                    ),
                    center = center3,
                    radius = 190f * cloudPulse
                ),
                radius = 190f * cloudPulse,
                center = center3
            )
        }

        Column(
            modifier = Modifier
                .scale(scaleAnim.value)
                .alpha(alphaAnim.value)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.ic_starscript_logo),
                contentDescription = "Starscript Logo",
                modifier = Modifier
                    .size(92.dp)
                    .padding(bottom = 12.dp)
            )
            Text(
                text = stringResource(R.string.app_name),
                style = WordmarkStyle
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Ultra-vivid, colorful, glowing "by vk"
            Text(
                text = stringResource(R.string.wordmark_subtitle),
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    letterSpacing = 4.5.sp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFFFC83D), // Warm gold
                            Color(0xFFFF5376), // Electric rose
                            Color(0xFFB347FF), // Vivid neon purple
                            Color(0xFF00E5FF), // Neon cyan
                            Color(0xFF2FD4A7)  // Emerald teal
                        )
                    ),
                    shadow = Shadow(
                        color = Color(0xFFB347FF).copy(alpha = 0.75f),
                        blurRadius = 24f,
                        offset = Offset(0f, 0f)
                    )
                )
            )
        }
    }
}
