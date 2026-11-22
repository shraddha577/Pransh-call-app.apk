package com.example.data.ai

import com.example.model.AssistantPersona
import com.example.model.DeviceTelemetry
import kotlinx.coroutines.delay

/**
 * Placeholder implementation used when Firebase AI Logic is not enabled.
 * Returns clearly labeled sample responses without making external API calls or using API keys.
 */
class PlaceholderAiRepository : AiRepository {
  override suspend fun generateReply(
    prompt: String,
    persona: AssistantPersona,
    userName: String,
    telemetry: DeviceTelemetry,
  ): AiResponse {
    delay(400)
    val q = prompt.trim().lowercase()
    val prefix = "[Sample AI Mode — Firebase Disabled]"

    val body =
      when {
        q.contains("hello") || q.contains("hi") || q.contains("hey") || q.contains("wake up") ->
          when (persona) {
            AssistantPersona.NOVA ->
              "At your service, $userName. All local HUD subsystems are online. Battery is at ${telemetry.batteryPercent}% and network link is ${telemetry.networkLabel}. Try voice commands like \"Open YouTube\", \"Battery status\", or \"Search Kotlin Compose\"."
            AssistantPersona.ORION ->
              "Online, $userName. Local telemetry verified: ${telemetry.deviceModel} running Android ${telemetry.androidVersion}. Issue a device command or enable Firebase AI Logic for live cloud reasoning."
            AssistantPersona.VEGA ->
              "Hey $userName! All sensors green at ${telemetry.batteryPercent}% battery. Ready to launch apps, search the web, or run diagnostics."
          }
        q.contains("pransh") || q.contains("developer") || q.contains("creator") || q.contains("channel") ->
          "This assistant experience is inspired by @pransh_official_coding's voice assistant builds. You can open their YouTube channel directly from the System tab or by saying \"Open Pransh channel\"."
        q.contains("who are you") || q.contains("your name") ->
          "I am ${persona.displayName}, your tactical mobile copilot. Right now I am running in local Sample AI mode; enable Firebase in AI Studio to connect live Gemini AI and Google Sign-In cloud sync."
        q.contains("code") || q.contains("kotlin") || q.contains("python") || q.contains("help") ->
          "Sample coding tip for \"$prompt\": Keep UI state reactive with StateFlow, isolate hardware and device intents behind clean controllers, and run async work inside viewModelScope coroutines."
        q.contains("weather") || q.contains("news") || q.contains("tell me about") ->
          "Live web and AI knowledge for \"$prompt\" is unavailable in Sample AI mode (requires Firebase AI Logic). However, I can launch a real Google Search for it—try saying \"Search $prompt\"."
        else ->
          when (persona) {
            AssistantPersona.NOVA ->
              "Acknowledged, $userName: \"$prompt\". In Sample AI mode, live Gemini responses require enabling Firebase. Meanwhile, native commands like \"Open YouTube\", \"Open Settings\", \"Battery status\", \"Search ...\", and \"Set timer\" run directly on your device."
            AssistantPersona.ORION ->
              "Processed locally: \"$prompt\". Cloud intelligence awaits Firebase activation. Local device telemetry (${telemetry.batteryPercent}% battery, ${telemetry.networkLabel}) and native Android command intents remain operational."
            AssistantPersona.VEGA ->
              "Got it, $userName! \"$prompt\" was handled in Sample AI mode. Enable Firebase to unlock live Gemini answers and Google Sign-In sync, or tap any quick command in the Commands tab."
          }
      }

    return AiResponse(
      text = "$prefix\n$body",
      isSamplePlaceholder = true,
    )
  }
}
