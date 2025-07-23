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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.visionassist.R
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DashboardScreen(userName: String, onModuleSelected: (Module) -> Unit) {
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
        // Animated background particles
        BackgroundParticles()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Welcome Section with animation
            WelcomeHeader(userName)
            Spacer(modifier = Modifier.height(40.dp))

            // Dashboard Grid
            DashboardGrid(onModuleSelected)
        }
    }
}

@Composable
private fun BackgroundParticles() {
    val infiniteTransition = rememberInfiniteTransition()

    // Create animations for each particle
    val particleAnimations = List(20) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 6.28f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000 + index * 100, easing = LinearEasing)
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                // Draw subtle floating particles in background
                particleAnimations.forEachIndexed { index, animation ->
                    val alpha = (sin(animation.value) + 1) / 2 * 0.1f

                    drawCircle(
                        color = Color(0xFF00FF7F).copy(alpha = alpha),
                        radius = 2f,
                        center = Offset(
                            (size.width * 0.1f * (index % 10)).coerceIn(0f, size.width),
                            (size.height * 0.05f * (index / 2)).coerceIn(0f, size.height)
                        )
                    )
                }
            }
    )
}

@Composable
private fun WelcomeHeader(userName: String) {
    val infiniteTransition = rememberInfiniteTransition()
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        )
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Animated underline effect
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(4.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF00FF7F),
                            Color.Transparent
                        )
                    )
                )
                .drawBehind {
                    val waveHeight = 2f
                    val waveLength = size.width / 4
                    val centerY = size.height / 2

                    drawLine(
                        color = Color(0xFF00FF7F),
                        start = Offset(0f, centerY),
                        end = Offset(size.width, centerY),
                        strokeWidth = 2f
                    )
                }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Welcome back, $userName!",
            color = Color(0xFFADFFB0),
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.scale(1f + 0.02f * sin(waveOffset))
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "How may I assist you today?",
            color = Color(0xFF88FFCC),
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtle animated indicator
        Box(
            modifier = Modifier
                .size(8.dp)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF00FF7F),
                                Color(0xFF00FF7F).copy(alpha = 0.5f),
                                Color.Transparent
                            )
                        )
                    )
                }
        )
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