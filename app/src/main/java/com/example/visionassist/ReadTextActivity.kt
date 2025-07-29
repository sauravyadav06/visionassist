package com.example.visionassist // Adjust package name if needed

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.visionassist.ui.design.ReadTextScreen // Import the Composable
import com.example.visionassist.ui.theme.VisionassistTheme
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.camera.core.CameraSelector

// Add these imports for Text Recognition and Image Analysis
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
// For handling image rotation
import android.media.ExifInterface
import android.util.Size
import java.io.ByteArrayOutputStream
import android.graphics.ImageFormat
import androidx.camera.core.CameraInfo

class ReadTextActivity : ComponentActivity() {

    companion object {
        private const val TAG = "ReadTextActivity"
    }

    // For requesting camera permission
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d(TAG, "Camera permission granted")
            startCamera()
        } else {
            Log.e(TAG, "Camera permission denied")
            Toast.makeText(this, "Camera permission is required for text reading.", Toast.LENGTH_LONG).show()
            // Optionally, finish the activity if permission is critical
            // finish()
        }
    }

    // CameraX related variables
    private var cameraExecutor: ExecutorService? = null
    private var previewView: PreviewView? = null
    private var cameraProvider: ProcessCameraProvider? = null

    // --- Text Recognition related variables (MOVED OUT OF companion object) ---
    // Text recognizer client (can be an instance variable)
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    // State for the currently detected text (should be instance variables)
    private var currentDetectedText: String = ""
    private var isProcessingImage: Boolean = false
    // --- Compose State Holder for UI updates (ADDED) ---
    private val _detectedTextState = mutableStateOf("Point camera at text...")
    val detectedText: String
        get() = _detectedTextState.value

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the camera executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        setContent {
            VisionassistTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ReadTextScreenContent(
                        onNavigateBack = { finish() },
                        detectedText = detectedText, // Pass the detected text state
                        // Pass the callback to receive the PreviewView reference
                        onPreviewViewCreated = { previewViewRef ->
                            previewView = previewViewRef
                            // Start camera setup only after we have the PreviewView and permission
                            if (ContextCompat.checkSelfPermission(
                                    this,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                startCamera()
                            } else {
                                requestCameraPermission()
                            }
                        }
                    )
                }
            }
        }
        // Initial permission check/request is handled via the callback now
    }

    private fun requestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d(TAG, "Camera permission already granted")
                startCamera() // Start camera if permission was already granted
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    // --- CameraX Setup Logic ---

    private fun startCamera() {
        val previewView = previewView ?: return

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            try {
                // CameraProvider is now guaranteed to be available
                cameraProvider = cameraProviderFuture.get()

                // Build and bind the camera use cases
                bindCameraUseCases()
            } catch (e: Exception) {
                Log.e(TAG, "Error getting camera provider", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraUseCases() {
        val cameraProvider = cameraProvider ?: return.also { Log.e(TAG, "CameraProvider is null") }
        val previewView = previewView ?: return.also { Log.e(TAG, "PreviewView is null") }

        // Create the Preview use case
        val preview = Preview.Builder().build().apply {
            setSurfaceProvider(previewView.surfaceProvider)
        }

        // Create the ImageAnalysis use case for text recognition
        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(1280, 720)) // Set target resolution
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) // Handle backpressure
            .build()

        // Set the analyzer for ImageAnalysis
        imageAnalysis.setAnalyzer(cameraExecutor!!, TextAnalyzer())

        // Select the back camera
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            // Unbind any existing use cases
            cameraProvider.unbindAll()

            // Check if the required camera exists and bind the use cases
            if (cameraProvider.hasCamera(cameraSelector)) {
                cameraProvider.bindToLifecycle(
                    this as LifecycleOwner, // LifecycleOwner
                    cameraSelector,        // CameraSelector
                    preview,               // Preview UseCase
                    imageAnalysis          // ImageAnalysis UseCase
                )
                Log.d(TAG, "Camera use cases bound successfully")
            } else {
                Log.e(TAG, "Back camera not available")
                Toast.makeText(this, "Back camera not found", Toast.LENGTH_SHORT).show()
            }

        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    // Add this inner class for analyzing camera frames
    inner class TextAnalyzer : ImageAnalysis.Analyzer {
        override fun analyze(imageProxy: ImageProxy) {
            // Avoid processing if already processing or if activity is finishing
            if (isProcessingImage || isFinishing) {
                imageProxy.close()
                return
            }

            isProcessingImage = true

            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                // Create InputImage from ImageProxy
                val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                val inputImage = InputImage.fromMediaImage(mediaImage, rotationDegrees)

                // Process the image with ML Kit Text Recognition
                textRecognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        // Task completed successfully
                        processTextRecognitionResult(visionText)
                    }
                    .addOnFailureListener { e ->
                        // Task failed with an exception
                        Log.e(TAG, "Text recognition failed", e)
                    }
                    .addOnCompleteListener {
                        // Always close the image proxy to release resources
                        imageProxy.close()
                        isProcessingImage = false
                    }
            } else {
                imageProxy.close()
                isProcessingImage = false
            }
        }
    }

    // Add this function to process the text recognition results
    private fun processTextRecognitionResult(visionText: Text) {
        // Get the most relevant block of text (you can adjust this logic)
        val detectedTextBuilder = StringBuilder()
        for (block in visionText.textBlocks) {
            // Append block text and a newline
            detectedTextBuilder.append(block.text).append("\n")
        }

        val detectedText = detectedTextBuilder.toString().trim()

        // Only update if text has changed significantly
        if (detectedText.isNotEmpty() && detectedText != currentDetectedText) {
            currentDetectedText = detectedText
            Log.d(TAG, "Detected Text: $currentDetectedText")

            // Update the UI state on the main thread (ADDED runOnUiThread)
            runOnUiThread {
                _detectedTextState.value = currentDetectedText
            }

            // TODO: Announce text via TTS (we'll do this later)
        }
    }

    // --- Composable Content (UPDATED SIGNATURE) ---
    @Composable
    private fun ReadTextScreenContent(
        onNavigateBack: () -> Unit,
        detectedText: String, // Add this parameter to pass detected text
        onPreviewViewCreated: (PreviewView) -> Unit
    ) {
        ReadTextScreen(
            onBackRequested = onNavigateBack,
            onPreviewViewCreated = onPreviewViewCreated,
            detectedText = detectedText // Pass it through to the UI composable
        )
    }


    // --- Cleanup ---

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor?.shutdown()
        // Consider unbinding camera use cases if needed for more complex scenarios
        // Shutdown the text recognizer when the activity is destroyed
        textRecognizer.close()
    }
}