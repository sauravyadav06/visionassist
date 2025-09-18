package com.example.visionassist

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import com.example.visionassist.design.ObjectDetectionScreen
import com.example.visionassist.logic.ObjectDetectionLogic
import com.google.mlkit.vision.common.InputImage

class ObjectDetectionActivity : ComponentActivity() {
    private lateinit var previewView: PreviewView
    private lateinit var objectLogic: ObjectDetectionLogic

    private val scanStatus = mutableStateOf("Initializing...")
    private val isPaused = mutableStateOf(false)

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            setupAnalyzer()
        } else {
            scanStatus.value = "Camera permission denied. Please enable it in Settings."
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        objectLogic = ObjectDetectionLogic(this) { label ->
            scanStatus.value = label
        }

        setContent {
            ObjectDetectionScreen(
                onPreviewReady = {
                    previewView = it
                    checkAndRequestCameraPermission()
                },
                scanStatus = scanStatus.value,
                isPaused = isPaused.value,
                onPauseToggle = { isPaused.value = !isPaused.value }
            )
        }
    }

    private fun checkAndRequestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            setupAnalyzer()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun setupAnalyzer() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            // Added backpressure strategy for better real-time performance
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analysis.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
                if (!isPaused.value) {
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )
                        // This is the crucial change: pass imageProxy to the logic class
                        objectLogic.processImage(image, imageProxy)
                    } else {
                        // Close the image if mediaImage is null
                        imageProxy.close()
                    }
                } else {
                    // Close the image if detection is paused
                    imageProxy.close()
                }
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, analysis)
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            isPaused.value = !isPaused.value
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        objectLogic.shutdown()
    }
}