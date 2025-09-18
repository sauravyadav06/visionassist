package com.example.visionassist.logic

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import java.util.Locale

class ObjectDetectionLogic(
    private val context: Context,
    private val onObjectDetected: (String) -> Unit
) {
    private lateinit var tts: TextToSpeech
    private var lastSpoken = ""

    private val options = ObjectDetectorOptions.Builder()
        .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
        .enableClassification()
        .build()

    private val objectDetector = ObjectDetection.getClient(options)

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    Log.e("TTS", "Language not supported")
                }
            } else {
                Log.e("TTS", "Initialization failed")
            }
        }
    }

    // Now accepting ImageProxy as a parameter
    @OptIn(ExperimentalGetImage::class)
    fun processImage(image: InputImage, imageProxy: ImageProxy) {
        objectDetector.process(image)
            .addOnSuccessListener { results ->
                if (results.isEmpty()) return@addOnSuccessListener

                val labels = results.map { obj ->
                    obj.labels.firstOrNull()?.text ?: "Unknown object"
                }

                val outputText = labels.groupingBy { it }.eachCount()
                    .entries.joinToString(" ") { (label, count) -> "$count $label" }

                if (outputText != lastSpoken && outputText.isNotBlank()) {
                    speakOut(outputText)
                    lastSpoken = outputText
                }

                onObjectDetected(outputText)
            }
            .addOnFailureListener { e ->
                Log.e("ObjectDetection", "Detection failed", e)
            }
            .addOnCompleteListener {
                // This is the crucial fix: close the ImageProxy after the task is complete
                imageProxy.close()
            }
    }

    fun speakOut(text: String) {
        if (::tts.isInitialized) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    fun shutdown() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
    }
}