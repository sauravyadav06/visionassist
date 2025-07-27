// File: app/src/main/java/com/example/visionassist/ui/design/ObjectDetectionScreen.kt

package com.example.visionassist.ui.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource // <-- Add this import for drawables
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.visionassist.R // <-- Import your R class to access drawables

@Composable
fun ObjectDetectionScreen(onBackRequested: () -> Unit) {
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
            // Header with Back Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackRequested) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFFADFFB0)
                    )
                }
                Text(
                    text = "Object Detection",
                    color = Color(0xFFADFFB0),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(32.dp))

            // Main Content Area (Placeholder for Camera Preview)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF002B2B)) // Darker background for preview area
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Use your vector drawable here
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // --- CHANGED THIS PART ---
                    Icon(
                        painter = painterResource(id = R.drawable.ic_camera), // <-- Use your drawable
                        contentDescription = "Camera Preview Area",
                        tint = Color(0xFF00FF7F), // You can adjust or remove tint if your SVG handles colors
                        modifier = Modifier.size(64.dp)
                    )
                    // --- END CHANGE ---
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Camera Preview & Object Detection Overlay will appear here",
                        color = Color(0xFF88FFCC),
                        fontSize = 16.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Initializing camera...",
                        color = Color(0xFF00FF7F),
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Controls (Placeholder)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { /* TODO: Implement Start Detection */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00FF7F),
                        contentColor = Color.Black
                    )
                ) {
                    Text("Start Detection")
                }
                Button(
                    onClick = { /* TODO: Implement Stop Detection */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF88FFCC),
                        contentColor = Color.Black
                    )
                ) {
                    Text("Stop")
                }
            }

            Spacer(Modifier.height(16.dp))

            // Status Text (Placeholder)
            Text(
                text = "Ready",
                color = Color(0xFFADFFB0),
                fontSize = 16.sp
            )
        }
    }
}