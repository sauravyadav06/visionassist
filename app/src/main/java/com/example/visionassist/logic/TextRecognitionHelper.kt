package com.example.visionassist.logic

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

class TextRecognitionHelper {

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun recognizeTextFromBitmap(bitmap: Bitmap): String {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val result = textRecognizer.process(image).await()
            extractTextFromResult(result)
        } catch (e: Exception) {
            Log.e("TextRecognitionHelper", "Error recognizing text", e)
            ""
        }
    }

    private fun extractTextFromResult(result: Text): String {
        val detectedText = StringBuilder()

        for (block in result.textBlocks) {
            for (line in block.lines) {
                detectedText.append(line.text).append(" ")
            }
            detectedText.append("\n")
        }

        return detectedText.toString().trim()
    }

    fun close() {
        textRecognizer.close()
    }
}