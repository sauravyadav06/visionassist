package com.example.visionassist.logic

import android.content.Context
import android.graphics.*
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.*
import kotlin.math.max
import kotlin.math.min

class ObjectDetectionLogic(
    private val context: Context,
    private val onObjectDetected: (List<DetectionResult>) -> Unit,
    private val onStatusUpdate: (String) -> Unit
) {
    private lateinit var tts: TextToSpeech
    private var lastSpoken = ""
    private val labels = getLabels()
    private val interpreter: Interpreter

    private val inputSize = 320 // change to 640 if your YOLOv5 model is 640x640
    private val threshold = 0.5f

    init {
        interpreter = Interpreter(loadModelFile("model.tflite"))

        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale.US
            }
        }
    }

    private fun loadModelFile(modelName: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelName)
        val inputStream = fileDescriptor.createInputStream()
        val fileChannel = inputStream.channel
        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            fileDescriptor.startOffset,
            fileDescriptor.declaredLength
        )
    }

    @OptIn(ExperimentalGetImage::class)
    fun processImage(imageProxy: ImageProxy) {
        try {
            val bitmap = imageProxy.toBitmap()
            val tensorImage = TensorImage.fromBitmap(bitmap)

            val processor = ImageProcessor.Builder()
                .add(ResizeOp(inputSize, inputSize, ResizeOp.ResizeMethod.BILINEAR))
                .add(NormalizeOp(0f, 255f)) // normalize [0,255] → [0,1]
                .build()

            val processedImage = processor.process(tensorImage)

            val outputShape = interpreter.getOutputTensor(0).shape() // [1,6300,85]
            val numDetections = outputShape[1]
            val numClasses = outputShape[2] - 5

            val output = Array(1) { Array(numDetections) { FloatArray(5 + numClasses) } }
            interpreter.run(processedImage.buffer, output)

            val detections = mutableListOf<DetectionResult>()

            for (i in 0 until numDetections) {
                val row = output[0][i]
                val confidence = row[4]
                if (confidence < threshold) continue

                val classes = row.copyOfRange(5, row.size)
                val classId = classes.indices.maxByOrNull { classes[it] } ?: -1
                val classScore = classes[classId]

                val score = confidence * classScore
                if (score < threshold) continue

                val cx = row[0] * bitmap.width
                val cy = row[1] * bitmap.height
                val w = row[2] * bitmap.width
                val h = row[3] * bitmap.height

                val left = max(0f, cx - w / 2f)
                val top = max(0f, cy - h / 2f)
                val right = min(bitmap.width.toFloat(), cx + w / 2f)
                val bottom = min(bitmap.height.toFloat(), cy + h / 2f)

                detections.add(
                    DetectionResult(
                        Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt()),
                        labels.getOrElse(classId) { "Unknown" }
                    )
                )
            }

            // ✅ Apply NMS
            val finalDetections = nms(detections, iouThreshold = 0.45f)
            onObjectDetected(finalDetections)

            val spokenText = finalDetections.groupingBy { it.label }.eachCount()
                .entries.joinToString(" ") { (label, count) -> "$count $label" }

            if (spokenText.isNotBlank() && spokenText != lastSpoken) {
                speakOut(spokenText)
                lastSpoken = spokenText
            }

            onStatusUpdate(if (finalDetections.isEmpty()) "No objects detected." else spokenText)

        } catch (e: Exception) {
            Log.e("YOLOv5", "Inference failed", e)
            onStatusUpdate("Detection failed: ${e.localizedMessage}")
        } finally {
            imageProxy.close()
        }
    }

    private fun getLabels(): List<String> {
        val labelsList = mutableListOf<String>()
        try {
            val reader = BufferedReader(InputStreamReader(context.assets.open("labels.txt")))
            reader.useLines { lines ->
                lines.forEach { line -> labelsList.add(line) }
            }
        } catch (e: Exception) {
            Log.e("YOLOv5", "Error reading labels.txt", e)
        }
        return labelsList
    }

    private fun speakOut(text: String) {
        if (::tts.isInitialized && !tts.isSpeaking) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    fun shutdown() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
    }

    // --- Helper: Non-Maximum Suppression (NMS) ---
    private fun nms(detections: List<DetectionResult>, iouThreshold: Float): List<DetectionResult> {
        val results = mutableListOf<DetectionResult>()
        val sorted = detections.sortedByDescending { it.boundingBox.width() * it.boundingBox.height() }
        val active = BooleanArray(sorted.size) { true }

        for (i in sorted.indices) {
            if (!active[i]) continue
            val current = sorted[i]
            results.add(current)

            for (j in i + 1 until sorted.size) {
                if (!active[j]) continue
                val iou = boxIou(current.boundingBox, sorted[j].boundingBox)
                if (iou > iouThreshold && current.label == sorted[j].label) {
                    active[j] = false
                }
            }
        }
        return results
    }

    private fun boxIou(a: Rect, b: Rect): Float {
        val interLeft = max(a.left, b.left)
        val interTop = max(a.top, b.top)
        val interRight = min(a.right, b.right)
        val interBottom = min(a.bottom, b.bottom)

        val interArea = max(0, interRight - interLeft) * max(0, interBottom - interTop)
        val unionArea = a.width() * a.height() + b.width() * b.height() - interArea

        return if (unionArea == 0) 0f else interArea.toFloat() / unionArea.toFloat()
    }
}

/**
 * Extension to convert ImageProxy to Bitmap
 */
fun ImageProxy.toBitmap(): Bitmap {
    val yBuffer = planes[0].buffer
    val uBuffer = planes[1].buffer
    val vBuffer = planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)
    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
    val out = java.io.ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
    val yuvByteArray = out.toByteArray()
    return BitmapFactory.decodeByteArray(yuvByteArray, 0, yuvByteArray.size)
}
