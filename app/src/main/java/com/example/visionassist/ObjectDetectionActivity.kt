package com.example.visionassist

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import com.example.visionassist.ui.design.ObjectDetectionScreen // We'll create this next
import com.example.visionassist.ui.theme.VisionassistTheme
//import java.lang.reflect.Modifier
import androidx.compose.ui.Modifier


class ObjectDetectionActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("ObjectDetectionActivity", "Camera permission granted")
            // Permission granted, camera setup can proceed (handled in Composable)
        } else {
            Log.e("ObjectDetectionActivity", "Camera permission denied")
            Toast.makeText(this, "Camera permission is required for object detection.", Toast.LENGTH_LONG).show()
            // Optionally, finish the activity or show an error screen
            // finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            VisionassistTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ObjectDetectionScreenContent()
                }
            }
        }

        // Request camera permission
        requestCameraPermission()
    }

    private fun requestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d("ObjectDetectionActivity", "Camera permission already granted")
                // Permission already granted, proceed with setup
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    @Composable
    private fun ObjectDetectionScreenContent() {
        // Pass context or necessary callbacks if needed for permission checks within Composable
        // For now, we'll just display the screen
        ObjectDetectionScreen(
            onBackRequested = { finish() } // Provide a way to go back
        )
    }
}