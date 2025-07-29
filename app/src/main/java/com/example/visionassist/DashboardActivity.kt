package com.example.visionassist


import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.visionassist.design.DashboardScreen
import com.example.visionassist.design.Module
import com.example.visionassist.logic.SimpleTTS
import com.example.visionassist.ui.theme.VisionassistTheme
import java.util.*

class DashboardActivity : ComponentActivity() {

    private lateinit var dashboardTTS: SimpleTTS
    private var speechRecognizer: SpeechRecognizer? = null
    private lateinit var vibrator: Vibrator
    private var userName: String = ""
    private var isListening = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userName = intent.getStringExtra("USER_NAME") ?: "User"

        // Initialize separate TTS for dashboard
        dashboardTTS = SimpleTTS(this)
        dashboardTTS.initialize()

        // Initialize speech recognizer
        initializeSpeechRecognizer()

        // Initialize vibrator
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        setContent {
            DashboardActivityContent()
        }

        // Start welcome message after a delay
        android.os.Handler(mainLooper).postDelayed({
            welcomeUserAndListModules()
        }, 2000)
    }

    private fun initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    isListening = false
                }

                override fun onError(error: Int) {
                    isListening = false
                    val errorMessage = getErrorMessage(error)
                    Log.e("DashboardActivity", "Speech recognition error: $errorMessage")
                    dashboardTTS.speak("Sorry, I didn't catch that. Please say Object Detection, Call Contacts, Read Text, or Weather.") {
                        android.os.Handler(mainLooper).postDelayed({
                            startVoiceCommandRecognition()
                        }, 500)
                    }
                }

                override fun onResults(results: Bundle?) {
                    isListening = false
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val result = matches?.getOrNull(0) ?: ""
                    handleVoiceCommand(result)
                }

                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
    }

    // In the DashboardActivityContent composable, update the call:
    @Composable
    private fun DashboardActivityContent() {
        VisionassistTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                DashboardScreen(
                    userName = userName,
                    isListening = isListening, // Add this line
                    onModuleSelected = { module ->
                        handleModuleSelection(module)
                    }
                )
            }
        }
    }



    private fun welcomeUserAndListModules() {
        try {
            val welcomeMessage = "Welcome to Vision Assist dashboard, $userName. " +
                    "I can assist you with Object Detection, Call Contacts, Read Text, or Weather. " +
                    "Please say which module you want to use."

            dashboardTTS.speak(welcomeMessage) {
                // Start listening after a small delay
                android.os.Handler(mainLooper).postDelayed({
                    startVoiceCommandRecognition()
                }, 500)
            }
        } catch (e: Exception) {
            Log.e("DashboardActivity", "Error in welcome message: ${e.message}")
        }
    }

    private fun startVoiceCommandRecognition() {
        if (isListening) return

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        try {
            speechRecognizer?.startListening(intent)
            isListening = true
        } catch (e: Exception) {
            Log.e("DashboardActivity", "Error starting voice recognition: ${e.message}")
            isListening = false
        }
    }

    private fun handleVoiceCommand(command: String) {
        try {
            val lowerCommand = command.lowercase()

            when {
                lowerCommand.contains("object") && (lowerCommand.contains("detection") || lowerCommand.contains("detect")) -> {
                    activateModule(Module.OBJECT_DETECTION, "Object Detection activated")
                }
                lowerCommand.contains("call") && lowerCommand.contains("contact") -> {
                    activateModule(Module.CALL_CONTACTS, "Call Contacts activated")
                }
                lowerCommand.contains("read") && lowerCommand.contains("text") -> {
                    activateModule(Module.READ_TEXT, "Read Text activated")
                }
                lowerCommand.contains("weather") -> {
                    activateModule(Module.WEATHER, "Weather activated")
                }
                else -> {
                    dashboardTTS.speak("I didn't understand. Please choose from Object Detection, Call Contacts, Read Text, or Weather.") {
                        android.os.Handler(mainLooper).postDelayed({
                            startVoiceCommandRecognition()
                        }, 500)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("DashboardActivity", "Error handling voice command: ${e.message}")
        }
    }

    private fun activateModule(module: Module, confirmationMessage: String) {
        try {
            // Vibrate to confirm activation
            vibrate()

            // Speak confirmation using dashboard TTS
            dashboardTTS.speak(confirmationMessage) {
                // Navigate after delay
                android.os.Handler(mainLooper).postDelayed({
                    navigateToModule(module)
                }, 1000)
            }
        } catch (e: Exception) {
            Log.e("DashboardActivity", "Error activating module: ${e.message}")
        }
    }

    private fun vibrate() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrationEffect = VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(vibrationEffect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(200)
            }
        } catch (e: Exception) {
            Log.e("DashboardActivity", "Error during vibration: ${e.message}")
        }
    }

    private fun navigateToModule(module: Module) {
        try {
            when (module) {
                Module.OBJECT_DETECTION -> {
                    // --- REPLACE THE TODO LINE ---
                    // TODO: Start object detection activity
                    // --- WITH THIS CODE ---
                    val intent = Intent(this, ObjectDetectionActivity::class.java)
                    startActivity(intent)
                    // Optional: Call finish() if you don't want Dashboard in the back stack
                    // finish()
                }
                Module.CALL_CONTACTS -> {
                    // TODO: Start call contacts activity
                }
                Module.READ_TEXT -> { // <-- Updated Section
                    val intent = Intent(this, ReadTextActivity::class.java)
                    startActivity(intent)
                    // Optional: Call finish() if you don't want Dashboard in the back stack
                    // finish()
                }
                Module.WEATHER -> {
                    // TODO: Start weather activity
                }
            }
        } catch (e: Exception) {
            Log.e("DashboardActivity", "Error navigating to module: ${e.message}")
        }
    }

    // Also handle manual module selection from UI
    private fun handleModuleSelection(module: Module) {
        try {
            val moduleName = when (module) {
                Module.OBJECT_DETECTION -> "Object Detection"
                Module.CALL_CONTACTS -> "Call Contacts"
                Module.READ_TEXT -> "Read Text"
                Module.WEATHER -> "Weather"
            }

            activateModule(module, "$moduleName activated")
        } catch (e: Exception) {
            Log.e("DashboardActivity", "Error handling module selection: ${e.message}")
        }
    }

    private fun getErrorMessage(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Unknown error occurred (code: $errorCode)"
        }
    }

    override fun onDestroy() {
        try {
            // Stop any ongoing speech recognition
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.e("DashboardActivity", "Error stopping speech recognizer: ${e.message}")
        }

        try {
            // Destroy speech recognizer
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.e("DashboardActivity", "Error destroying speech recognizer: ${e.message}")
        }

        try {
            // Destroy dashboard TTS
            dashboardTTS.destroy()
        } catch (e: Exception) {
            Log.e("DashboardActivity", "Error destroying dashboard TTS: ${e.message}")
        }

        super.onDestroy()
    }
}