package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.CrimsonAccent
import com.example.ui.theme.VioletAccent

enum class AssistantPersona(
  val displayName: String,
  val subtitle: String,
  val accentColor: Color,
  val defaultPitch: Float,
  val defaultRate: Float,
) {
  NOVA(
    displayName = "N.O.V.A.",
    subtitle = "Tactical HUD & Precision AI",
    accentColor = ArcCyan,
    defaultPitch = 0.96f,
    defaultRate = 1.04f,
  ),
  ORION(
    displayName = "O.R.I.O.N.",
    subtitle = "Deep Core Command AI",
    accentColor = CrimsonAccent,
    defaultPitch = 0.80f,
    defaultRate = 0.96f,
  ),
  VEGA(
    displayName = "V.E.G.A.",
    subtitle = "Rapid Neural Copilot",
    accentColor = VioletAccent,
    defaultPitch = 1.10f,
    defaultRate = 1.08f,
  ),
}

data class LocalUserProfile(
  val uid: String,
  val displayName: String,
  val email: String,
  val isGoogleLinked: Boolean = false,
)

data class DeviceTelemetry(
  val batteryPercent: Int,
  val isCharging: Boolean,
  val networkLabel: String,
  val deviceModel: String,
  val androidVersion: String,
  val localTime: String,
  val localDate: String,
)

enum class MessageRole {
  USER,
  ASSISTANT,
  DEVICE_ACTION,
}

enum class VoiceStatus {
  IDLE,
  LISTENING,
  THINKING,
  SPEAKING,
}
