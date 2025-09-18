    package com.example.visionassist.design

    import androidx.camera.view.PreviewView
    import androidx.compose.foundation.layout.*
    import androidx.compose.material3.*
    import androidx.compose.runtime.*
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.platform.LocalContext
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.viewinterop.AndroidView

    @Composable
    fun ObjectDetectionScreen(
        onPreviewReady: (PreviewView) -> Unit,
        scanStatus: String,
        isPaused: Boolean,
        onPauseToggle: () -> Unit
    ) {
        val context = LocalContext.current
        val previewView = remember { PreviewView(context) }

        LaunchedEffect(Unit) {
            onPreviewReady(previewView)
        }

        Box(modifier = Modifier.fillMaxSize()) {
            // Camera Preview
            AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

            // Controls
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