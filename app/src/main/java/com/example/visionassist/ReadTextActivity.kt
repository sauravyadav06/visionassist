package com.example.visionassist

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import com.example.visionassist.design.ReadTextScreen
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.*
import java.util.concurrent.Executors

class ReadTextActivity : ComponentActivity() {

    private val cameraExecutor by lazy { Executors.newSingleThreadExecutor() }
    private var cameraProvider: ProcessCameraProvider? = null
    private var analysisUseCase: ImageAnalysis? = null
    private var previewUseCase: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var previewView: PreviewView? = null

    private var tts: TextToSpeech? = null
    private var ttsReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize TTS
        tts = TextToSpeech(this) { status ->
            ttsReady = status == TextToSpeech.SUCCESS
            if (ttsReady) {
                tts?.language = Locale.getDefault()
                speak("Text reading mode activated. Point camera at text and press volume up to capture.")
            } else {
                Log.e("ReadTextActivity", "TTS initialization failed")
            }
        }

        // Initialize camera
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            requestCameraAndStart()
        }, ContextCompat.getMainExecutor(this))

        setContent {
            MaterialTheme {
                Surface {
                    var recognized by remember { mutableStateOf("") }
                    var speaking by remember { mutableStateOf(false) }
                    var captureStatus by remember { mutableStateOf("Ready to capture") }

                    ReadTextScreen(
                        recognizedText = recognized,
                        isSpeaking = speaking,
                        captureStatus = captureStatus,
                        onPreviewReady = { pv ->
                            previewView = pv
                            if (hasCameraPermission()) bindCameraUseCases()
                        },
                        onCapture = {
                            captureStatus = "Capturing..."
                            speak("Capturing text...", immediate = true)
                            captureAndReadText { success, text ->
                                if (success) {
                                    recognized = text
                                    speaking = true
                                    captureStatus = "Text captured"
                                } else {
                                    captureStatus = "No text found"
                                }
                            }
                        },
                        onStop = {
                            speaking = false
                            stopSpeak()
                            captureStatus = "Stopped"
                        }
                    )

                    DisposableEffect(Unit) {
                        onDispose {
                            // Cleanup handled in onDestroy
                        }
                    }
                }
            }
        }
    }

    private fun requestCameraAndStart() {
        if (hasCameraPermission()) {
            bindCameraUseCases()
        } else {
            speak("Camera permission required", true)
        }
    }

    private fun hasCameraPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED

    private fun bindCameraUseCases() {
        val provider = cameraProvider ?: return
        val pv = previewView ?: return

        provider.unbindAll()

        // Preview use case
        previewUseCase = Preview.Builder().build().also {
            it.setSurfaceProvider(pv.surfaceProvider)
        }

        // Image capture use case
        imageCapture = ImageCapture.Builder().build()

        // Analysis use case (optional, for continuous preview)
        val analyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        analysisUseCase = analyzer

        try {
            provider.bindToLifecycle(
                this,
                CameraSelector.DEFAULT_BACK_CAMERA,
                previewUseCase,
                imageCapture,
                analysisUseCase
            )
        } catch (e: Exception) {
            Log.e("ReadTextActivity", "Use case binding failed", e)
        }
    }

    private fun captureAndReadText(callback: (Boolean, String) -> Unit) {
        val imageCaptureUseCase = imageCapture ?: run {
            speak("Camera not ready. Please try again.", true)
            callback(false, "")
            return
        }

        // Take a picture
        imageCaptureUseCase.takePicture(
            cameraExecutor,
            object : ImageCapture.OnImageCapturedCallback() {
                @OptIn(ExperimentalGetImage::class)
                override fun onCaptureSuccess(image: ImageProxy) {
                    try {
                        val rotation = image.imageInfo.rotationDegrees
                        val mediaImage = image.image

                        if (mediaImage != null) {
                            val inputImage = InputImage.fromMediaImage(mediaImage, rotation)
                            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

                            recognizer.process(inputImage)
                                .addOnSuccessListener { result ->
                                    val rawText = result.text
                                    val normalizedText = rawText.replace(Regex("\\s+"), " ").trim()

                                    if (normalizedText.isNotEmpty()) {
                                        speak("Text captured: $normalizedText", true)
                                        callback(true, normalizedText)
                                    } else {
                                        speak("No text found. Please try again.", true)
                                        callback(false, "")
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    Log.e("ReadTextActivity", "Text recognition failed", exception)
                                    speak("Error capturing text. Please try again.", true)
                                    callback(false, "")
                                }
                                .addOnCompleteListener {
                                    recognizer.close()
                                }
                        } else {
                            speak("No image captured. Please try again.", true)
                            callback(false, "")
                        }
                    } finally {
                        image.close()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("ReadTextActivity", "Image capture failed", exception)
                    speak("Error capturing image. Please try again.", true)
                    callback(false, "")
                }
            }
        )
    }

    private fun speak(text: String, immediate: Boolean = false) {
        if (!ttsReady) {
            Log.w("ReadTextActivity", "TTS not ready: $text")
            return
        }

        if (!immediate) {
            tts?.stop()
        }

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "read_text")
    }

    private fun stopSpeak() {
        tts?.stop()
    }

    // Volume key controls for blind users
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                speak("Capturing text...", true)
                captureAndReadText { success, text -> }
                true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                stopSpeak()
                speak("Stopped speaking", true)
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        tts?.shutdown()
    }
}