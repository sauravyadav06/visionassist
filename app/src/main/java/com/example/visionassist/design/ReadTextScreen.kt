package com.example.visionassist.ui.design // Adjust package name if needed

import android.view.ViewGroup
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Composable UI for the Read Text screen.
 *
 * @param onBackRequested Callback when the back button is pressed.
 * @param onPreviewViewCreated Callback to provide the PreviewView instance to the Activity.
 * @param detectedText The text detected by ML Kit to display.
 * @param translatedText The text translated by ML Kit to display.
 */
@Composable
fun ReadTextScreen(
    onBackRequested: () -> Unit,
    onPreviewViewCreated: ((PreviewView) -> Unit)? = null,
    detectedText: String = "Point camera at text...", // Parameter for detected text
    translatedText: String = "Say 'Translate to [language]'" // --- ADD THIS PARAMETER ---
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF001F1F)) // Dark teal background
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Read Text",
                color = Color(0xFF00FF7F), // Bright teal
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onBackRequested) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF00FF7F)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // --- Camera Preview Area (Integrated with CameraX) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp) // Adjust height as needed
                .background(Color.Black)
        ) {
            AndroidView(
                factory = { context ->
                    val previewView = PreviewView(context)
                    previewView.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    // Optional: Set scale type
                    // previewView.scaleType = PreviewView.ScaleType.FILL_CENTER

                    // Invoke the callback to pass the PreviewView reference to the Activity
                    onPreviewViewCreated?.invoke(previewView)

                    previewView // Return the created PreviewView
                },
                update = { /* Update logic if needed */ },
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(Modifier.height(16.dp))

        // Detected Text Display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF003333)) // Darker teal card
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Detected Text:",
                    color = Color(0xFF00FF7F),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(4.dp))
                // Display the actual detected text passed as a parameter
                Text(
                    text = detectedText,
                    color = Color(0xFF88FFCC), // Lighter teal
                    fontSize = 14.sp
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // --- Translation Display (UPDATED TO USE translatedText PARAMETER) ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF003333))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Translation:",
                    color = Color(0xFF00FF7F),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(4.dp))
                // Display the actual translated text passed as a parameter
                Text(
                    text = translatedText, // --- USE THE translatedText PARAMETER HERE ---
                    color = Color(0xFF88FFCC),
                    fontSize = 14.sp
                )
            }
        }
        // --- END UPDATE ---

        Spacer(Modifier.height(16.dp))

        // Controls (Placeholder actions for now)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { /* TODO: Implement Start Reading Logic */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00FF7F),
                    contentColor = Color.Black
                )
            ) {
                Text("Start Reading")
            }
            Button(
                onClick = { /* TODO: Implement Stop Reading Logic */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF88FFCC),
                    contentColor = Color.Black
                )
            ) {
                Text("Stop")
            }
        }

        Spacer(Modifier.height(16.dp))

        // Voice Command Hint
        Text(
            text = "Say: 'Translate to [language]'",
            color = Color(0xFF00FF7F).copy(alpha = 0.8f),
            fontSize = 14.sp
        )
    }
}