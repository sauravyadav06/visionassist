package com.example.visionassist.logic

import android.graphics.Rect

data class DetectionResult(
    val boundingBox: Rect,
    val label: String
)
