package com.example.visionassist.ui.design
//
//import androidx.compose.animation.core.*
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.res.colorResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.Dp
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.example.visionassist.R
//import androidx.compose.ui.res.painterResource
//
//
//@Composable
//fun WelcomeScreen(isListening: Boolean, userName: String?) {
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(
//                brush = Brush.verticalGradient(
//                    colors = listOf(
//                        Color(android.graphics.Color.parseColor("#1A237E")), // Deep Blue
//                        Color(android.graphics.Color.parseColor("#4A148C"))  // Rich Purple
//                    )
//                )
//            ),
//        contentAlignment = Alignment.Center
//    ) {
//        Column(
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center,
//            modifier = Modifier.padding(24.dp)
//        ) {
//            Image(
//                painter = painterResource(id = R.drawable.mic),
//                contentDescription = null,
//                modifier = Modifier
//                    .size(80.dp)
//                    .clip(CircleShape)
//                    .background(Color.White.copy(alpha = 0.2f))
//                    .padding(20.dp),
//                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White)
//            )
//
//            Spacer(modifier = Modifier.height(32.dp))
//
//            Text(
//                text = when {
//                    userName != null -> "Hello, $userName!"
//                    isListening -> "Say your name now…"
//                    else -> "Welcome to VisionAssist"
//                },
//                color = Color.White,
//                fontSize = 28.sp,
//                fontWeight = FontWeight.Bold,
//                textAlign = TextAlign.Center
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            Text(
//                text = when {
//                    userName != null -> "Getting things ready…"
//                    isListening -> "I'm listening carefully…"
//                    else -> "One moment please."
//                },
//                color = colorResource(id = R.color.text_secondary),
//                fontSize = 16.sp,
//                textAlign = TextAlign.Center
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            if (isListening || userName != null) {
//                RunningDotsIndicator()
//            }
//        }
//    }
//}
//
//@Composable
//private fun RunningDotsIndicator() {
//    val infiniteTransition = rememberInfiniteTransition()
//
//    @Composable
//    fun dotAnimation(delay: Int): Dp {
//        return infiniteTransition.animateFloat(
//            initialValue = 0f,
//            targetValue = 1f,
//            animationSpec = infiniteRepeatable(
//                animation = keyframes {
//                    durationMillis = 1500
//                    0.0f at delay with LinearEasing
//                    1.0f at (delay + 500) with LinearEasing
//                },
//                repeatMode = RepeatMode.Restart
//            )
//        ).value
//            .let { scale ->
//                if (scale < 0.5) 8.dp else 12.dp
//            }
//    }
//
//    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
//        Box(Modifier.size(dotAnimation(0)).clip(CircleShape).background(Color(0xFFBB86FC)))
//        Box(Modifier.size(dotAnimation(200)).clip(CircleShape).background(Color(0xFFBB86FC)))
//        Box(Modifier.size(dotAnimation(400)).clip(CircleShape).background(Color(0xFFBB86FC)))
//    }
//}

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun WelcomeScreen(isListening: Boolean, userName: String?) {
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
            ),
        contentAlignment = Alignment.Center
    ) {
        // Draw particles on the entire screen background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    // Get the center of the entire screen
                    val screenWidth = size.width
                    val screenHeight = size.height
                    val screenCenter = Offset(screenWidth / 2, screenHeight / 2)

                    // Draw all particles relative to screen center
                    particleManager.particles.forEach { particle ->
                        drawCircle(
                            color = Color(0xFF00FF7F).copy(alpha = particle.alpha),
                            radius = particle.size,
                            center = Offset(
                                screenCenter.x + particle.offsetX,
                                screenCenter.y + particle.offsetY
                            )
                        )
                    }
                }
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            if (isListening) {
                GlowingRingEffect()
                Spacer(modifier = Modifier.height(32.dp))
            }

            Text(
                text = when {
                    userName != null -> "Hello, $userName!"
                    isListening -> "Say your name now…"
                    else -> "Welcome to VisionAssist"
                },
                color = Color(0xFFADFFB0), // Soft neon green
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = when {
                    userName != null -> "Getting things ready…"
                    isListening -> "I'm listening carefully…"
                    else -> "One moment please."
                },
                color = Color(0xFF88FFCC), // Lighter green
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isListening || userName != null) {
                RunningDotsIndicator()
            }
        }
    }
}

// Global particle manager to handle particles across the entire screen
@Suppress("ObjectPropertyName")
private val particleManager = ParticleManager()

class ParticleManager {
    var particles by mutableStateOf<List<Particle>>(emptyList())
        private set

    fun addParticle(particle: Particle) {
        particles = particles + particle
    }

    fun updateParticles() {
        val updatedParticles = particles.map { particle ->
            particle.copy(
                offsetX = particle.offsetX + particle.speedX,
                offsetY = particle.offsetY + particle.speedY,
                alpha = particle.alpha - 0.015f
            )
        }.filter { it.alpha > 0f }

        particles = updatedParticles
    }
}

@Composable
private fun GlowingRingEffect() {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Generate new particles
    LaunchedEffect(Unit) {
        while (true) {
            if (particleManager.particles.size < 30) { // More particles for better effect
                val angle = Random.nextFloat() * 360f
                val speed = Random.nextFloat() * 8f + 4f // Faster speed to reach corners
                val radians = Math.toRadians(angle.toDouble()).toFloat()

                val newParticle = Particle(
                    id = System.currentTimeMillis() + Random.nextLong(0, 1000),
                    offsetX = 0f,
                    offsetY = 0f,
                    scale = Random.nextFloat() * 0.5f + 0.3f,
                    alpha = 1f,
                    speedX = Math.cos(radians.toDouble()).toFloat() * speed,
                    speedY = Math.sin(radians.toDouble()).toFloat() * speed,
                    size = Random.nextFloat() * 8f + 4f
                )
                particleManager.addParticle(newParticle)
            }
            delay(80) // More frequent particle generation
        }
    }

    // Update existing particles
    LaunchedEffect(Unit) {
        while (true) {
            particleManager.updateParticles()
            delay(30) // Faster updates for smoother movement
        }
    }

    Box(
        modifier = Modifier
            .size(300.dp)
            .scale(scale)
            .drawBehind {
                // Draw main glowing circle
                drawGlowCircle()
            }
    )
}

// Particle data class
data class Particle(
    val id: Long,
    val offsetX: Float,
    val offsetY: Float,
    val scale: Float,
    val alpha: Float,
    val speedX: Float,
    val speedY: Float,
    val size: Float
)

// ✅ FIXED: Extension function on DrawScope
private fun DrawScope.drawGlowCircle() {
    val radius = size.minDimension / 2
    val center = Offset(size.width / 2, size.height / 2)
    val glowColor = Color(0xFF00FF7F)

    // Outer blurred glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                glowColor.copy(alpha = 0.6f),
                Color.Transparent
            ),
            center = center,
            radius = radius
        ),
        center = center,
        radius = radius
    )

    // Inner bright glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                glowColor.copy(alpha = 0.9f),
                glowColor.copy(alpha = 0.3f),
                Color.Transparent
            ),
            center = center,
            radius = radius * 0.7f
        ),
        center = center,
        radius = radius * 0.7f
    )

    // Additional inner core for more intensity
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                glowColor.copy(alpha = 1.0f),
                glowColor.copy(alpha = 0.7f),
                Color.Transparent
            ),
            center = center,
            radius = radius * 0.4f
        ),
        center = center,
        radius = radius * 0.4f
    )
}

// Pulsing dots indicator
@Composable
fun RunningDotsIndicator() {
    val infiniteTransition = rememberInfiniteTransition()

    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        DotBox(infiniteTransition, 0)
        DotBox(infiniteTransition, 200)
        DotBox(infiniteTransition, 400)
    }
}

@Composable
fun DotBox(infiniteTransition: InfiniteTransition, delay: Int) {
    val scale = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1500
                0.0f at delay with LinearEasing
                1.0f at (delay + 500) with LinearEasing
            },
            repeatMode = RepeatMode.Restart
        )
    ).value

    val size = if (scale < 0.5f) 8.dp else 12.dp

    Box(
        Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.85f))
    )
}