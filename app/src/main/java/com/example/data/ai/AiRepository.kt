package com.example.data.ai

import com.example.model.AssistantPersona
import com.example.model.DeviceTelemetry

data class AiResponse(
  val text: String,
  val isSamplePlaceholder: Boolean = true,
)

interface AiRepository {
  suspend fun generateReply(
    prompt: String,
    persona: AssistantPersona,
    userName: String,
    telemetry: DeviceTelemetry,
  ): AiResponse
}
