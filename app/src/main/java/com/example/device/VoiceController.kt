package com.example.device

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG = "VoiceController"

class VoiceController(
  private val context: Context,
  private val onSpeechResult: (String) -> Unit,
  private val onSpeechError: (String) -> Unit,
) {
  private var tts: TextToSpeech? = null
  private var ttsReady = false
  private var speechRecognizer: SpeechRecognizer? = null

  private val _isListening = MutableStateFlow(false)
  val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

  private val _isSpeaking = MutableStateFlow(false)
  val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

  private val _rmsLevel = MutableStateFlow(0f)
  val rmsLevel: StateFlow<Float> = _rmsLevel.asStateFlow()

  private val _partialText = MutableStateFlow("")
  val partialText: StateFlow<String> = _partialText.asStateFlow()

  init {
    tts =
      TextToSpeech(context.applicationContext) { status ->
        if (status == TextToSpeech.SUCCESS) {
          tts?.language = Locale.getDefault()
          tts?.setOnUtteranceProgressListener(
            object : UtteranceProgressListener() {
              override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
              }

              override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
              }

              @Deprecated("Deprecated in Java")
              override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
              }
            }
          )
          ttsReady = true
        } else {
          Log.w(TAG, "TextToSpeech init failed with status=$status")
        }
      }
  }

  fun speak(text: String, pitch: Float, rate: Float) {
    val clean =
      text
        .replace("[Sample AI Mode — Firebase Disabled]", "")
        .trim()
    if (!ttsReady || clean.isBlank()) return
    tts?.setPitch(pitch.coerceIn(0.6f, 1.4f))
    tts?.setSpeechRate(rate.coerceIn(0.7f, 1.4f))
    val utteranceId = UUID.randomUUID().toString()
    tts?.speak(clean, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
  }

  fun stopSpeaking() {
    tts?.stop()
    _isSpeaking.value = false
  }

  fun startListening() {
    if (!SpeechRecognizer.isRecognitionAvailable(context)) {
      onSpeechError("On-device speech recognition service is not installed on this device.")
      return
    }
    stopSpeaking()
    speechRecognizer?.destroy()
    val recognizer = SpeechRecognizer.createSpeechRecognizer(context.applicationContext)
    speechRecognizer = recognizer
    _partialText.value = ""

    recognizer.setRecognitionListener(
      object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
          _isListening.value = true
        }

        override fun onBeginningOfSpeech() {
          _isListening.value = true
        }

        override fun onRmsChanged(rmsdB: Float) {
          _rmsLevel.value = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
          _isListening.value = false
          _rmsLevel.value = 0f
        }

        override fun onError(error: Int) {
          _isListening.value = false
          _rmsLevel.value = 0f
          _partialText.value = ""
          val msg =
            when (error) {
              SpeechRecognizer.ERROR_AUDIO -> "Audio recording error."
              SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required."
              SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected. Please try again."
              SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Listening timed out."
              SpeechRecognizer.ERROR_NETWORK,
              SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network error during speech recognition."
              else -> "Voice input unavailable right now (code $error)."
            }
          onSpeechError(msg)
        }

        override fun onResults(results: Bundle?) {
          _isListening.value = false
          _rmsLevel.value = 0f
          _partialText.value = ""
          val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
          val text = matches?.firstOrNull()?.trim().orEmpty()
          if (text.isNotEmpty()) {
            onSpeechResult(text)
          } else {
            onSpeechError("Could not recognize speech. Try typing your command.")
          }
        }

        override fun onPartialResults(partialResults: Bundle?) {
          val matches =
            partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
          _partialText.value = matches?.firstOrNull().orEmpty()
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
      }
    )

    val intent =
      Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(
          RecognizerIntent.EXTRA_LANGUAGE_MODEL,
          RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
        )
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
      }
    recognizer.startListening(intent)
  }

  fun stopListening() {
    speechRecognizer?.stopListening()
    _isListening.value = false
    _rmsLevel.value = 0f
    _partialText.value = ""
  }

  fun shutdown() {
    speechRecognizer?.destroy()
    speechRecognizer = null
    tts?.stop()
    tts?.shutdown()
    tts = null
  }
}
