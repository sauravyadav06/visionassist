package com.example.visionassist.logic

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.*

class SimpleTTS(private val context: Context) : TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false
    private var onTtsReady: (() -> Unit)? = null

    fun initialize(onReady: (() -> Unit)? = null) {
        onTtsReady = onReady
        textToSpeech = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            try {
                val result = textToSpeech?.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Language not supported")
                } else {
                    isInitialized = true
                    textToSpeech?.setSpeechRate(0.9f)
                    textToSpeech?.setPitch(1.0f)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error setting TTS properties: ${e.message}")
            }
        } else {
            Log.e(TAG, "TTS initialization failed with status: $status")
        }
        onTtsReady?.invoke()
    }

    fun speak(text: String, onCompleted: (() -> Unit)? = null) {
        if (!isInitialized) {
            onCompleted?.invoke()
            return
        }

        try {
            val utteranceId = "tts_${System.currentTimeMillis()}"

            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}

                override fun onDone(utteranceId: String?) {
                    onCompleted?.invoke()
                }

                override fun onError(utteranceId: String?) {
                    Log.e(TAG, "TTS Error for utterance: $utteranceId")
                    onCompleted?.invoke()
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    Log.e(TAG, "TTS Error for utterance: $utteranceId, code: $errorCode")
                    onCompleted?.invoke()
                }
            })

            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } catch (e: Exception) {
            Log.e(TAG, "Error speaking text: ${e.message}")
            onCompleted?.invoke()
        }
    }

    fun stop() {
        try {
            textToSpeech?.stop()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping TTS: ${e.message}")
        }
    }

    fun destroy() {
        try {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        } catch (e: Exception) {
            Log.e(TAG, "Error destroying TTS: ${e.message}")
        } finally {
            textToSpeech = null
            isInitialized = false
        }
    }

    companion object {
        private const val TAG = "SimpleTTS"
    }
}