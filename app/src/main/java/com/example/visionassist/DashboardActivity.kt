
package com.example.visionassist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.visionassist.design.DashboardScreen
import com.example.visionassist.design.Module

class DashboardActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get user name from intent
        val userName = intent.getStringExtra("USER_NAME") ?: "User"

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                DashboardActivityContent(userName)
            }
        }
    }

    @Composable
    private fun DashboardActivityContent(userName: String) {
        DashboardScreen(
            userName = userName,
            onModuleSelected = { module ->
                handleModuleSelection(module)
            }
        )
    }

    private fun handleModuleSelection(module: Module) {
        when (module) {
            Module.OBJECT_DETECTION -> {
                // TODO: Start object detection functionality
                // Example: startActivity(Intent(this, ObjectDetectionActivity::class.java))
            }
            Module.CALL_CONTACTS -> {
                // TODO: Start call contacts functionality
            }
            Module.READ_TEXT -> {
                // TODO: Start read text functionality
            }
            Module.WEATHER -> {
                // TODO: Start weather functionality
            }
        }
    }
}