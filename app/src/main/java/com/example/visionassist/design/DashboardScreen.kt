package com.example.visionassist.design

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.visionassist.R


@Composable
fun DashboardScreen(
    userName: String,
    isListening: Boolean = false,
    onModuleSelected: (Module) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF001F1F), // Deep dark green
                        Color.Black       // Pure black edges
                    ),
                    center = Offset.Unspecified,
                    radius = 800f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Welcome Section
            WelcomeHeader(userName)
            Spacer(modifier = Modifier.height(20.dp))

            // Voice Listening Indicator
            VoiceListeningIndicator(isListening)
            Spacer(modifier = Modifier.height(20.dp))

            // Dashboard Grid
            DashboardGrid(onModuleSelected)
        }
    }
}

@Composable
private fun WelcomeHeader(userName: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Welcome back, $userName!",
            color = Color(0xFFADFFB0), // Soft neon green
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "How may I assist you today?",
            color = Color(0xFF88FFCC), // Lighter green
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun VoiceListeningIndicator(isListening: Boolean) {
    if (isListening) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Pulsing Microphone Animation
            PulsingMicrophone()
            Spacer(Modifier.height(12.dp))

            // Listening Status Text
            Text(
                text = "Listening... Speak now",
                color = Color(0xFF00FF7F),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(8.dp))

            // Waveform Visualization
            AudioWaveform()
        }
    } else {
        // Show ready status when not listening
        Text(
            text = "Ready to listen",
            color = Color(0xFF88FFCC).copy(alpha = 0.7f),
            fontSize = 14.sp
        )
    }
}

@Composable
private fun PulsingMicrophone() {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .size(40.dp)
            .scale(scale)
            .drawBehind {
                // Microphone icon drawing
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF00FF7F),
                            Color(0xFF00AA55)
                        )
                    )
                )

                // Microphone stand
                drawRect(
                    color = Color(0xFF00FF7F),
                    topLeft = Offset(size.width / 2 - 2.dp.toPx(), size.height * 0.6f),
                    size = androidx.compose.ui.geometry.Size(4.dp.toPx(), 12.dp.toPx())
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Microphone grille
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.Black)
        )
    }
}

@Composable
private fun AudioWaveform() {
    val infiniteTransition = rememberInfiniteTransition()

    // Create multiple animated bars for waveform effect
    val barHeights = List(7) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(800 + index * 100, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        barHeights.forEach { heightAnimation ->
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height((20 * heightAnimation.value).dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF00FF7F))
            )
        }
    }
}

@Composable
private fun DashboardGrid(onModuleSelected: (Module) -> Unit) {
    val modules = listOf(
        DashboardModule("Object Detection", R.drawable.ic_camera, Module.OBJECT_DETECTION),
        DashboardModule("Call Contacts", R.drawable.ic_call, Module.CALL_CONTACTS),
        DashboardModule("Read Text", R.drawable.ic_read, Module.READ_TEXT),
        DashboardModule("Weather", R.drawable.ic_weather, Module.WEATHER)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        items(modules) { module ->
            DashboardModuleCard(module, onModuleSelected)
        }
    }
}

@Composable
private fun DashboardModuleCard(
    module: DashboardModule,
    onModuleSelected: (Module) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val hoverEffect by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF002B2B),
                        Color(0xFF001F1F)
                    )
                )
            )
            .clickable {
                onModuleSelected(module.moduleType)
            }
            .drawBehind {
                // Glow effect
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF00FF7F).copy(alpha = 0.3f * hoverEffect),
                            Color.Transparent
                        )
                    ),
                    center = Offset(size.width / 2, size.height / 2),
                    radius = size.maxDimension
                )

                // Border glow
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF00FF7F).copy(alpha = 0.5f * hoverEffect),
                            Color(0xFF00FF7F).copy(alpha = 0.2f * hoverEffect)
                        )
                    ),
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(20.dp.toPx())
                )
            }
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon with pulsing effect
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF00FF7F).copy(alpha = 0.4f),
                                    Color(0xFF00FF7F).copy(alpha = 0.1f),
                                    Color.Transparent
                                )
                            )
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = module.iconResId),
                    contentDescription = null,
                    tint = Color(0xFF00FF7F),
                    modifier = Modifier
                        .size(30.dp)
                        .scale(1f + 0.1f * hoverEffect)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = module.title,
                color = Color(0xFFADFFB0),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Subtle underline for each module
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(2.dp)
                    .background(Color(0xFF00FF7F).copy(alpha = 0.5f))
            )
        }
    }
}

// Data classes
data class DashboardModule(
    val title: String,
    val iconResId: Int,
    val moduleType: Module
)

enum class Module {
    OBJECT_DETECTION,
    CALL_CONTACTS,
    READ_TEXT,
    WEATHER
}