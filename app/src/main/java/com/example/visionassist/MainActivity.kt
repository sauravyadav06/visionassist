package com.example.visionassist

import VoiceAssistantWelcome
import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import com.example.visionassist.ui.design.WelcomeScreen
import com.example.visionassist.ui.theme.VisionassistTheme

class MainActivity : ComponentActivity() {

    private var voiceAssistantWelcome: VoiceAssistantWelcome? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            GreetingFlow()
        }
    }

    @Composable
    private fun GreetingFlow() {
        var isListening by remember { mutableStateOf(false) }
        var userName by remember { mutableStateOf<String?>(null) }
        var permissionGranted by remember { mutableStateOf(false) }

        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                if (isGranted) {
                    permissionGranted = true  // ✅ Update state → triggers LaunchedEffect
                } else {
                    Toast.makeText(this, "Mic access required!", Toast.LENGTH_LONG).show()
                }
            }
        )

        // ✅ Launch permission request
        LaunchedEffect(Unit) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        // ✅ Run ONLY when permission is granted
        LaunchedEffect(permissionGranted) {
            if (permissionGranted) {
                voiceAssistantWelcome = VoiceAssistantWelcome(context = this@MainActivity).apply {
                    initialize {
                        speak("Hello! Welcome to VisionAssist. May I know your good name to assist you?")
                        isListening = true

                        greetAndGetName { name ->
                            runOnUiThread {
                                userName = name
                                isListening = false
                            }

                            speak("Thank you, $name. Let me get things ready for you.") {
                                openCameraScreen(name)
                            }
                        }
                    }
                }
            }
        }

        // ✅ Cleanup
        DisposableEffect(Unit) {
            onDispose {
                voiceAssistantWelcome?.shutdown()
            }
        }

        // ✅ Show UI
        VisionassistTheme() {
            WelcomeScreen(isListening = isListening, userName = userName)
        }
    }

    private fun openCameraScreen(userName: String) {
        startActivity(
            Intent(this, DashboardActivity::class.java).putExtra("USER_NAME", userName)
        )
        finish()
    }

    override fun onDestroy() {
        voiceAssistantWelcome?.shutdown()
        super.onDestroy()
    }
}

