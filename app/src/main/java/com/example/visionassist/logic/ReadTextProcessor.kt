package com.example.visionassist.logic

import android.content.Context
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

class ReadTextProcessor(
    context: Context,
    private val onNewFrame: (ImageProxy) -> Unit
) : ImageAnalysis.Analyzer {

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        // Just pass the frame to the activity for storage
        // We don't process it here to avoid automatic readings
        onNewFrame(imageProxy)
        // Note: The activity will close the image proxy
    }

    fun close() {
        // Cleanup if needed
    }
}