// app/src/main/java/com/vk/clockdin/ui/navigation/IntroScreen.kt
package com.vk.clockdin.ui.navigation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun IntroScreen(onFinished: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    var exiting by remember { mutableStateOf(false) }

    // Animate in
    LaunchedEffect(Unit) {
        delay(120)
        visible = true
        delay(1800) // hold visible
        exiting = true
        delay(500)  // fade-out duration
        onFinished()
    }

    val scale by animateFloatAsState(
        targetValue = if (visible && !exiting) 1f else if (!visible) 0.88f else 1.04f,
        animationSpec = if (!exiting)
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        else tween(500),
        label = "intro_scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible && !exiting) 1f else 0f,
        animationSpec = tween(if (exiting) 500 else 400),
        label = "intro_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF090A0F), Color(0xFF000000))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .alpha(alpha)
        ) {
            Text(
                text = "clockdin",
                fontSize = 52.sp,
                fontWeight = FontWeight.Thin,
                color = Color.White.copy(alpha = 0.92f),
                letterSpacing = 6.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "by vk",
                fontSize = 16.sp,
                fontWeight = FontWeight.Light,
                color = Color.White.copy(alpha = 0.38f),
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
internal fun PlaceholderScreen(label: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090A0F)),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White.copy(alpha = 0.4f))
    }
}