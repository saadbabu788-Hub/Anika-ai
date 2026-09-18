package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.data.model.OrbState
import java.util.Locale

class AnikaVoiceEngine(
    private val context: Context,
    private val onStateChanged: (OrbState) -> Unit,
    private val onSpeechRecognized: (String) -> Unit,
    private val onWakeWordDetected: () -> Unit
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false

    init {
        tts = TextToSpeech(context.applicationContext, this)
        initSpeechRecognizer()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            // Prefer Indian English or Hindi
            val indianLocale = Locale("en", "IN")
            val hindiLocale = Locale("hi", "IN")
            val availableLocale = when {
                tts?.isLanguageAvailable(indianLocale) == TextToSpeech.LANG_AVAILABLE -> indianLocale
                tts?.isLanguageAvailable(hindiLocale) == TextToSpeech.LANG_AVAILABLE -> hindiLocale
                else -> Locale.getDefault()
            }
            tts?.language = availableLocale
            tts?.setPitch(1.15f) // Friendly feminine tone
            tts?.setSpeechRate(0.98f) // Natural conversational tempo

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    onStateChanged(OrbState.SPEAKING)
                }

                override fun onDone(utteranceId: String?) {
                    onStateChanged(OrbState.IDLE)
                }

                override fun onError(utteranceId: String?) {
                    onStateChanged(OrbState.IDLE)
                }
            })
        }
    }

    private fun initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        onStateChanged(OrbState.LISTENING)
                    }

                    override fun onBeginningOfSpeech() {
                        onStateChanged(OrbState.LISTENING)
                    }

                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        onStateChanged(OrbState.PROCESSING)
                    }

                    override fun onError(error: Int) {
                        isListening = false
                        onStateChanged(OrbState.IDLE)
                    }

                    override fun onResults(results: Bundle?) {
                        isListening = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val spokenText = matches?.firstOrNull() ?: ""
                        if (spokenText.isNotBlank()) {
                            // Check wake word
                            val lower = spokenText.lowercase(Locale.ROOT)
                            if (lower.contains("hey anika") || lower.startsWith("anika")) {
                                onWakeWordDetected()
                            }
                            onSpeechRecognized(spokenText)
                        } else {
                            onStateChanged(OrbState.IDLE)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        }
    }

    fun startListening() {
        if (speechRecognizer == null) initSpeechRecognizer()
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        try {
            speechRecognizer?.startListening(intent)
            isListening = true
            onStateChanged(OrbState.LISTENING)
        } catch (e: Exception) {
            isListening = false
            onStateChanged(OrbState.ERROR)
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            isListening = false
            onStateChanged(OrbState.IDLE)
        } catch (e: Exception) {
            // Ignored
        }
    }

    fun speak(text: String, enabled: Boolean = true) {
        if (!enabled || !isTtsReady) return
        stopListening()
        tts?.stop()
        // Strip out emojis for cleaner TTS narration
        val cleanText = text.replace(Regex("""[\p{So}\p{Cn}]"""), "")
        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "ANIKA_SPEECH_${System.currentTimeMillis()}")
    }

    fun stopSpeaking() {
        tts?.stop()
        onStateChanged(OrbState.IDLE)
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        speechRecognizer?.destroy()
    }
}
