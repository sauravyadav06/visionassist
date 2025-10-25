package com.example.visionassist.design

import android.graphics.Paint
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.visionassist.logic.DetectionResult

@Composable
fun ObjectDetectionScreen(
    onPreviewReady: (PreviewView) -> Unit,
    scanStatus: String,
    isPaused: Boolean,
    onPauseToggle: () -> Unit,
    detectionResults: List<DetectionResult>
) {
    val context = LocalContext.current
    val previewView = remember { PreviewView(context) }
    val density = LocalDensity.current.density

    var previewSize by remember { mutableStateOf(Size.Zero) }

    LaunchedEffect(Unit) {
        onPreviewReady(previewView)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                previewView.post {
                    previewSize = Size(
                        previewView.width.toFloat(),
                        previewView.height.toFloat()
                    )
                }
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            if (previewSize.width > 0 && previewSize.height > 0) {
                val scaleX = size.width / previewSize.width
                val scaleY = size.height / previewSize.height

                detectionResults.forEach { result ->
                    val box = result.boundingBox
                    val scaledRect = androidx.compose.ui.geometry.Rect(
                        left = box.left * scaleX,
                        top = box.top * scaleY,
                        right = box.right * scaleX,
                        bottom = box.bottom * scaleY
                    )

                    drawRect(
                        color = Color.Green,
                        topLeft = scaledRect.topLeft,
                        size = scaledRect.size,
                        style = Stroke(width = 5.dp.toPx())
                    )

                    drawContext.canvas.nativeCanvas.drawText(
                        result.label,
                        scaledRect.left,
                        scaledRect.top - 10,
                        Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 40f
                        }
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = scanStatus,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isPaused) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary
            )

            Button(onClick = onPauseToggle) {
                Text(if (isPaused) "Resume Detection" else "Pause Detection")
            }

            Text(
                text = "Press Volume Up to pause or resume",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}