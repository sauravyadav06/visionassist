package com.example.visionassist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.ui.unit.sp

class CameraActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userName = intent.getStringExtra("USER_NAME") ?: "Explorer"

        setContent {
            Text("Hello $userName! Ready for object detection.", fontSize = 20.sp)
        }
    }
}