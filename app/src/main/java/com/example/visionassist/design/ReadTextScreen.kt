package com.example.visionassist.design

import android.content.Context
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun ReadTextScreen(
    recognizedText: String,
    isSpeaking: Boolean,
    captureStatus: String,
    onPreviewReady: (PreviewView) -> Unit,
    onCapture: () -> Unit,
    onStop: () -> Unit
) {
    val context: Context = LocalContext.current
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    LaunchedEffect(Unit) { onPreviewReady(previewView) }

    Box(Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Semi-transparent overlay with controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f))
                .padding(12.dp)
        ) {
            Text(
                text = if (recognizedText.isNotBlank()) recognizedText else "Point camera at text...",
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = captureStatus,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = onCapture) {
                    Text("Capture Text")
                }
                OutlinedButton(onClick = onStop) {
                    Text("Stop Speaking")
                }
                AssistChip(
                    onClick = {},
                    label = { Text(if (isSpeaking) "Speaking" else "Ready") }
                )
            }

            Text(
                text = "Press Volume Up to capture, Volume Down to stop",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}