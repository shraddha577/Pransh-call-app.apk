package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AssistantPersona
import com.example.model.LocalUserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.jarvisPrefsDataStore: DataStore<Preferences> by
  preferencesDataStore(name = "jarvis_session_prefs")

data class UserSettings(
  val profile: LocalUserProfile?,
  val persona: AssistantPersona = AssistantPersona.NOVA,
  val autoSpeak: Boolean = true,
  val speechRate: Float = 1.05f,
  val speechPitch: Float = 0.95f,
)

class SessionPreferences(private val context: Context) {
  private object Keys {
    val IS_SIGNED_IN = booleanPreferencesKey("is_signed_in")
    val USER_ID = stringPreferencesKey("user_id")
    val USER_NAME = stringPreferencesKey("user_name")
    val USER_EMAIL = stringPreferencesKey("user_email")
    val PERSONA = stringPreferencesKey("persona")
    val AUTO_SPEAK = booleanPreferencesKey("auto_speak")
    val SPEECH_RATE = floatPreferencesKey("speech_rate")
    val SPEECH_PITCH = floatPreferencesKey("speech_pitch")
  }

  val settingsFlow: Flow<UserSettings> =
    context.jarvisPrefsDataStore.data.map { prefs ->
      val signedIn = prefs[Keys.IS_SIGNED_IN] ?: false
      val profile =
        if (signedIn) {
          LocalUserProfile(
            uid = prefs[Keys.USER_ID] ?: "local_commander",
            displayName = prefs[Keys.USER_NAME]?.ifBlank { "Commander" } ?: "Commander",
            email = prefs[Keys.USER_EMAIL].orEmpty(),
            isGoogleLinked = false,
          )
        } else {
          null
        }
      val persona =
        AssistantPersona.entries.firstOrNull { it.name == prefs[Keys.PERSONA] }
          ?: AssistantPersona.NOVA
      UserSettings(
        profile = profile,
        persona = persona,
        autoSpeak = prefs[Keys.AUTO_SPEAK] ?: true,
        speechRate = prefs[Keys.SPEECH_RATE] ?: persona.defaultRate,
        speechPitch = prefs[Keys.SPEECH_PITCH] ?: persona.defaultPitch,
      )
    }

  suspend fun signInLocal(name: String, email: String) {
    val cleanName = name.trim().ifBlank { "Commander" }
    val cleanEmail = email.trim()
    val uid = "local_${cleanName.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_').ifBlank { "user" }}"
    context.jarvisPrefsDataStore.edit { prefs ->
      prefs[Keys.IS_SIGNED_IN] = true
      prefs[Keys.USER_ID] = uid
      prefs[Keys.USER_NAME] = cleanName
      prefs[Keys.USER_EMAIL] = cleanEmail
    }
  }

  suspend fun signOut() {
    context.jarvisPrefsDataStore.edit { prefs ->
      prefs[Keys.IS_SIGNED_IN] = false
    }
  }

  suspend fun updatePersona(persona: AssistantPersona) {
    context.jarvisPrefsDataStore.edit { prefs ->
      prefs[Keys.PERSONA] = persona.name
      prefs[Keys.SPEECH_RATE] = persona.defaultRate
      prefs[Keys.SPEECH_PITCH] = persona.defaultPitch
    }
  }

  suspend fun updateAutoSpeak(enabled: Boolean) {
    context.jarvisPrefsDataStore.edit { prefs -> prefs[Keys.AUTO_SPEAK] = enabled }
  }

  suspend fun updateSpeechRate(rate: Float) {
    context.jarvisPrefsDataStore.edit { prefs -> prefs[Keys.SPEECH_RATE] = rate }
  }

  suspend fun updateSpeechPitch(pitch: Float) {
    context.jarvisPrefsDataStore.edit { prefs -> prefs[Keys.SPEECH_PITCH] = pitch }
  }
}
