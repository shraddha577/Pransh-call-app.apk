package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiRepository
import com.example.data.ai.PlaceholderAiRepository
import com.example.data.local.ChatMessageEntity
import com.example.data.local.CommandMacroEntity
import com.example.data.local.JarvisDatabase
import com.example.data.local.SessionPreferences
import com.example.data.local.UserSettings
import com.example.device.CommandResult
import com.example.device.DeviceCommandExecutor
import com.example.device.VoiceController
import com.example.model.AssistantPersona
import com.example.model.DeviceTelemetry
import com.example.model.LocalUserProfile
import com.example.model.MessageRole
import com.example.model.VoiceStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AuthUiState {
  data object Loading : AuthUiState

  data object SignedOut : AuthUiState

  data class SignedIn(val profile: LocalUserProfile) : AuthUiState
}

data class NovaHudState(
  val authState: AuthUiState = AuthUiState.Loading,
  val settings: UserSettings = UserSettings(profile = null),
  val messages: List<ChatMessageEntity> = emptyList(),
  val macros: List<CommandMacroEntity> = emptyList(),
  val telemetry: DeviceTelemetry,
  val voiceStatus: VoiceStatus = VoiceStatus.IDLE,
  val rmsLevel: Float = 0f,
  val partialTranscript: String = "",
  val showGoogleFirebaseDialog: Boolean = false,
  val statusBannerMessage: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class NovaViewModel(application: Application) : AndroidViewModel(application) {
  private val dao = JarvisDatabase.getInstance(application).jarvisDao()
  private val prefs = SessionPreferences(application)
  private val aiRepository: AiRepository = PlaceholderAiRepository()

  private val _telemetry =
    MutableStateFlow(DeviceCommandExecutor.readTelemetry(application))
  private val _isThinking = MutableStateFlow(false)
  private val _showGoogleFirebaseDialog = MutableStateFlow(false)
  private val _statusBannerMessage = MutableStateFlow<String?>(null)

  private val voiceController: VoiceController =
    VoiceController(
      context = application,
      onSpeechResult = { recognizedText -> sendPrompt(recognizedText) },
      onSpeechError = { errMsg -> _statusBannerMessage.value = errMsg },
    )

  private val messagesFlow =
    prefs.settingsFlow.flatMapLatest { settings ->
      val uid = settings.profile?.uid
      if (uid.isNullOrBlank()) flowOf(emptyList()) else dao.observeMessages(uid)
    }

  private val macrosFlow =
    prefs.settingsFlow.flatMapLatest { settings ->
      val uid = settings.profile?.uid
      if (uid.isNullOrBlank()) flowOf(emptyList()) else dao.observeMacros(uid)
    }

  private val voiceStatusFlow =
    combine(
      voiceController.isListening,
      _isThinking,
      voiceController.isSpeaking,
    ) { listening, thinking, speaking ->
      when {
        listening -> VoiceStatus.LISTENING
        thinking -> VoiceStatus.THINKING
        speaking -> VoiceStatus.SPEAKING
        else -> VoiceStatus.IDLE
      }
    }

  val uiState: StateFlow<NovaHudState> =
    combine(
      prefs.settingsFlow,
      messagesFlow,
      macrosFlow,
      _telemetry,
      voiceStatusFlow,
      voiceController.rmsLevel,
      voiceController.partialText,
      _showGoogleFirebaseDialog,
      _statusBannerMessage,
    ) { values ->
      @Suppress("UNCHECKED_CAST")
      val settings = values[0] as UserSettings
      @Suppress("UNCHECKED_CAST")
      val messages = values[1] as List<ChatMessageEntity>
      @Suppress("UNCHECKED_CAST")
      val macros = values[2] as List<CommandMacroEntity>
      val telemetry = values[3] as DeviceTelemetry
      val voiceStatus = values[4] as VoiceStatus
      val rms = values[5] as Float
      val partial = values[6] as String
      val showGoogleDialog = values[7] as Boolean
      val bannerMsg = values[8] as String?

      val auth =
        if (settings.profile != null) {
          AuthUiState.SignedIn(settings.profile)
        } else {
          AuthUiState.SignedOut
        }

      NovaHudState(
        authState = auth,
        settings = settings,
        messages = messages,
        macros = macros,
        telemetry = telemetry,
        voiceStatus = voiceStatus,
        rmsLevel = rms,
        partialTranscript = partial,
        showGoogleFirebaseDialog = showGoogleDialog,
        statusBannerMessage = bannerMsg,
      )
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = NovaHudState(telemetry = _telemetry.value),
    )

  fun signInLocal(name: String, email: String) {
    viewModelScope.launch {
      prefs.signInLocal(name, email)
      val cleanName = name.trim().ifBlank { "Commander" }
      val uid =
        "local_${cleanName.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_').ifBlank { "user" }}"
      seedDefaultMacrosIfEmpty(uid)
      seedWelcomeMessageIfEmpty(uid, cleanName)
    }
  }

  fun signOut() {
    voiceController.stopListening()
    voiceController.stopSpeaking()
    viewModelScope.launch { prefs.signOut() }
  }

  fun onGoogleSignInClicked() {
    _showGoogleFirebaseDialog.value = true
  }

  fun dismissGoogleFirebaseDialog() {
    _showGoogleFirebaseDialog.value = false
  }

  fun dismissStatusBanner() {
    _statusBannerMessage.value = null
  }

  fun showVoiceMessage(message: String) {
    _statusBannerMessage.value = message
  }

  fun refreshTelemetry() {
    _telemetry.value = DeviceCommandExecutor.readTelemetry(getApplication())
  }

  fun toggleListening() {
    if (voiceController.isListening.value) {
      voiceController.stopListening()
    } else {
      voiceController.startListening()
    }
  }

  fun stopSpeaking() {
    voiceController.stopSpeaking()
  }

  fun speakText(text: String) {
    val settings = uiState.value.settings
    voiceController.speak(text, settings.speechPitch, settings.speechRate)
  }

  fun sendPrompt(rawPrompt: String) {
    val prompt = rawPrompt.trim()
    val profile = uiState.value.settings.profile ?: return
    if (prompt.isEmpty()) return

    viewModelScope.launch {
      _statusBannerMessage.value = null
      dao.insertMessage(
        ChatMessageEntity(
          userId = profile.uid,
          role = MessageRole.USER.name,
          content = prompt,
        )
      )

      refreshTelemetry()
      val currentTelemetry = _telemetry.value
      val currentSettings = uiState.value.settings

      when (val cmd = DeviceCommandExecutor.tryExecuteCommand(getApplication(), prompt)) {
        is CommandResult.Executed -> {
          dao.insertMessage(
            ChatMessageEntity(
              userId = profile.uid,
              role = MessageRole.DEVICE_ACTION.name,
              content = cmd.summary,
              actionTag = cmd.actionTag,
              isSampleAi = false,
            )
          )
          if (currentSettings.autoSpeak) {
            voiceController.speak(
              cmd.summary,
              currentSettings.speechPitch,
              currentSettings.speechRate,
            )
          }
        }

        CommandResult.NotACommand -> {
          _isThinking.value = true
          try {
            val reply =
              aiRepository.generateReply(
                prompt = prompt,
                persona = currentSettings.persona,
                userName = profile.displayName,
                telemetry = currentTelemetry,
              )
            dao.insertMessage(
              ChatMessageEntity(
                userId = profile.uid,
                role = MessageRole.ASSISTANT.name,
                content = reply.text,
                isSampleAi = reply.isSamplePlaceholder,
              )
            )
            if (currentSettings.autoSpeak) {
              voiceController.speak(
                reply.text,
                currentSettings.speechPitch,
                currentSettings.speechRate,
              )
            }
          } finally {
            _isThinking.value = false
          }
        }
      }
    }
  }

  fun addCustomMacro(title: String, triggerPhrase: String, description: String, category: String) {
    val profile = uiState.value.settings.profile ?: return
    if (title.isBlank() || triggerPhrase.isBlank()) return
    viewModelScope.launch {
      dao.insertMacro(
        CommandMacroEntity(
          userId = profile.uid,
          title = title.trim(),
          triggerPhrase = triggerPhrase.trim(),
          description = description.trim().ifBlank { "Custom voice & tap command" },
          category = category.trim().ifBlank { "Custom" },
        )
      )
    }
  }

  fun deleteMacro(macroId: Long) {
    viewModelScope.launch { dao.deleteMacroById(macroId) }
  }

  fun clearHistory() {
    val profile = uiState.value.settings.profile ?: return
    viewModelScope.launch {
      dao.clearMessagesForUser(profile.uid)
      seedWelcomeMessageIfEmpty(profile.uid, profile.displayName)
    }
  }

  fun selectPersona(persona: AssistantPersona) {
    viewModelScope.launch { prefs.updatePersona(persona) }
  }

  fun setAutoSpeak(enabled: Boolean) {
    if (!enabled) voiceController.stopSpeaking()
    viewModelScope.launch { prefs.updateAutoSpeak(enabled) }
  }

  fun setSpeechRate(rate: Float) {
    viewModelScope.launch { prefs.updateSpeechRate(rate) }
  }

  fun setSpeechPitch(pitch: Float) {
    viewModelScope.launch { prefs.updateSpeechPitch(pitch) }
  }

  fun openCreatorChannel() {
    val opened =
      DeviceCommandExecutor.launchUrl(
        getApplication(),
        "https://youtube.com/@pransh_official?si=uWYpV_dCdMN5X-13",
      )
    if (!opened) {
      _statusBannerMessage.value = "No browser or YouTube app found to open @pransh_official."
    }
  }

  private suspend fun seedDefaultMacrosIfEmpty(uid: String) {
    if (dao.countMacrosForUser(uid) == 0) {
      dao.insertMacros(
        listOf(
          CommandMacroEntity(
            userId = uid,
            title = "Open YouTube",
            triggerPhrase = "Open YouTube",
            description = "Launches YouTube app or web player",
            category = "Apps",
          ),
          CommandMacroEntity(
            userId = uid,
            title = "System Diagnostics",
            triggerPhrase = "Battery status",
            description = "Reads live battery, network, and OS telemetry",
            category = "Telemetry",
          ),
          CommandMacroEntity(
            userId = uid,
            title = "Pransh Channel",
            triggerPhrase = "Open Pransh channel",
            description = "Opens @pransh_official on YouTube",
            category = "Creator",
          ),
          CommandMacroEntity(
            userId = uid,
            title = "System Settings",
            triggerPhrase = "Open settings",
            description = "Opens Android device settings",
            category = "System",
          ),
          CommandMacroEntity(
            userId = uid,
            title = "Search Web",
            triggerPhrase = "Search Android Jetpack Compose",
            description = "Runs a live Google web search",
            category = "Web",
          ),
          CommandMacroEntity(
            userId = uid,
            title = "1-Min Timer",
            triggerPhrase = "Set timer for 1 min",
            description = "Starts a 60-second countdown timer",
            category = "Clock",
          ),
        )
      )
    }
  }

  private suspend fun seedWelcomeMessageIfEmpty(uid: String, userName: String) {
    dao.insertMessage(
      ChatMessageEntity(
        userId = uid,
        role = MessageRole.ASSISTANT.name,
        content =
          "[Sample AI Mode — Firebase Disabled]\nWelcome online, $userName. Voice recognition, text-to-speech, and real Android device commands are active. Tap the Neural Core orb or type a command to begin.",
        isSampleAi = true,
      )
    )
  }

  override fun onCleared() {
    voiceController.shutdown()
    super.onCleared()
  }
}
