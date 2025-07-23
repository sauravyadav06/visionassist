package com.example.visionassist.logic

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.Toast
import java.util.*

class VoiceAssistant(private val context: Context) {

    private var textToSpeech: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var onNameReceived: ((String) -> Unit)? = null

    fun initialize(onReady: () -> Unit) {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.US
                textToSpeech?.setSpeechRate(0.9f)
                onReady()
            } else {
                postError("Failed to initialize TTS engine.")
            }
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : android.speech.RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val spoken = matches?.firstOrNull() ?: ""
                    handleSpokenText(spoken)
                }

                override fun onError(error: Int) {
                    val msg = when (error) {
                        SpeechRecognizer.ERROR_CLIENT -> "Permission denied"
                        SpeechRecognizer.ERROR_NETWORK -> "Network issue"
                        SpeechRecognizer.ERROR_NO_MATCH -> "Didn’t catch that"
                        else -> "Listening failed"
                    }
                    postError(msg)
                    retryListening()
                }

                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
    }

    private fun handleSpokenText(text: String) {
        val name = NameExtractor().extract(text)
        if (name.isNotEmpty()) {
            onNameReceived?.invoke(name)
        } else {
            postError("Couldn’t understand your name.")
            retryListening()
        }
    }

    private fun retryListening() {
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            startListening()
        }, 1000)
    }

    fun speak(text: String, onDone: (() -> Unit)? = null) {
        val utteranceId = "speak_${System.currentTimeMillis()}"

        textToSpeech?.setOnUtteranceCompletedListener { finishedId ->
            if (finishedId == utteranceId && onDone != null) {
                Handler(Looper.getMainLooper()).post(onDone)
            }
        }

        textToSpeech?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            utteranceId
        )
    }

    fun greetAndGetName(onNameFound: (String) -> Unit) {
        onNameReceived = onNameFound
        speak("Hello! Welcome to VisionAssist. May I know your good name to assist you?")
        startListening()
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Please say your name clearly.")
        }

        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            postError("Cannot listen: ${e.message}")
        }
    }

    fun shutdown() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        speechRecognizer?.destroy()
        textToSpeech = null
        speechRecognizer = null
        onNameReceived = null
    }

    private fun postError(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }
}