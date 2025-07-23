package com.example.visionassist.logic

class NameExtractor {
    fun extract(input: String): String {
        val trimmed = input.trim().lowercase()
        return when {
            trimmed.contains("i am ") -> trimmed.substringAfter("i am ").trim()
            trimmed.contains("my name is ") -> trimmed.substringAfter("my name is ").trim()
            trimmed.contains("call me ") -> trimmed.substringAfter("call me ").trim()
            trimmed.contains("this is ") -> trimmed.substringAfter("this is ").trim()
            else -> trimmed.split(" ").lastOrNull()?.trim().orEmpty()
        }.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString()
        }
    }
}