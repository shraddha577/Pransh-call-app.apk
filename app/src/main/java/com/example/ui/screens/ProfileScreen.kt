package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.AssistantPersona
import com.example.ui.NovaHudState
import com.example.ui.components.SampleAiNoticeBanner
import com.example.ui.theme.CrimsonAccent
import com.example.ui.theme.HudBorder
import com.example.ui.theme.HudCard
import com.example.ui.theme.HudElevated
import com.example.ui.theme.ObsidianBg

@Composable
fun ProfileScreen(
  state: NovaHudState,
  onGoogleSignInClick: () -> Unit,
  onSignOut: () -> Unit,
  onSelectPersona: (AssistantPersona) -> Unit,
  onToggleAutoSpeak: (Boolean) -> Unit,
  onSpeechRateChange: (Float) -> Unit,
  onSpeechPitchChange: (Float) -> Unit,
  onTestVoice: () -> Unit,
  onClearHistory: () -> Unit,
  onOpenCreatorChannel: () -> Unit,
) {
  val accent = state.settings.persona.accentColor
  val profile = state.settings.profile

  Box(
    modifier = Modifier.fillMaxSize().background(ObsidianBg),
    contentAlignment = Alignment.TopCenter,
  ) {
    LazyColumn(
      modifier = Modifier.widthIn(max = 840.dp).fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      item(key = "profile_header") {
        Text(
          text = "SYSTEM & PROFILE",
          style = MaterialTheme.typography.headlineMedium,
          color = MaterialTheme.colorScheme.onSurface,
        )
      }

      item(key = "profile_account_card") {
        Surface(
          shape = MaterialTheme.shapes.large,
          color = HudCard,
          border = BorderStroke(1.dp, HudBorder),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Surface(
                shape = CircleShape,
                color = accent.copy(alpha = 0.18f),
                modifier = Modifier.size(52.dp),
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "User profile avatar",
                    tint = accent,
                    modifier = Modifier.size(28.dp),
                  )
                }
              }
              Spacer(modifier = Modifier.width(14.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = profile?.displayName ?: "Commander",
                  style = MaterialTheme.typography.titleLarge,
                  color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                  text =
                    profile?.email?.ifBlank { "Offline Local Profile • No Cloud Sync" }
                      ?: "Offline Local Profile",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              OutlinedButton(
                onClick = onGoogleSignInClick,
                border = BorderStroke(1.dp, accent.copy(alpha = 0.7f)),
                modifier = Modifier.weight(1f).height(48.dp).testTag("profile_google_link_button"),
              ) {
                Icon(
                  imageVector = Icons.Filled.AccountCircle,
                  contentDescription = null,
                  tint = accent,
                  modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Google Login", maxLines = 1)
              }

              OutlinedButton(
                onClick = onSignOut,
                border = BorderStroke(1.dp, CrimsonAccent.copy(alpha = 0.65f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonAccent),
                modifier = Modifier.weight(1f).height(48.dp).testTag("sign_out_button"),
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.Logout,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Sign Out", maxLines = 1)
              }
            }
          }
        }
      }

      item(key = "sample_ai_notice_profile") {
        SampleAiNoticeBanner()
      }

      item(key = "persona_selector") {
        Surface(
          shape = MaterialTheme.shapes.large,
          color = HudCard,
          border = BorderStroke(1.dp, HudBorder),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Text(
              text = "ASSISTANT PERSONA",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurface,
            )

            AssistantPersona.entries.forEach { persona ->
              val selected = persona == state.settings.persona
              PersonaRow(
                persona = persona,
                selected = selected,
                onClick = { onSelectPersona(persona) },
              )
            }
          }
        }
      }

      item(key = "voice_settings") {
        Surface(
          shape = MaterialTheme.shapes.large,
          color = HudCard,
          border = BorderStroke(1.dp, HudBorder),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Auto-Speak Responses",
                  style = MaterialTheme.typography.titleMedium,
                  color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                  text = "Speak assistant replies and command confirmations aloud",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
              Switch(
                checked = state.settings.autoSpeak,
                onCheckedChange = onToggleAutoSpeak,
                colors =
                  SwitchDefaults.colors(
                    checkedThumbColor = ObsidianBg,
                    checkedTrackColor = accent,
                  ),
                modifier = Modifier.testTag("settings_autospeak_switch"),
              )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
              text = "Speech Rate: ${"%.2f".format(state.settings.speechRate)}x",
              style = MaterialTheme.typography.labelMedium,
              color = accent,
            )
            Slider(
              value = state.settings.speechRate,
              onValueChange = onSpeechRateChange,
              valueRange = 0.7f..1.4f,
              colors = SliderDefaults.colors(thumbColor = accent, activeTrackColor = accent),
              modifier = Modifier.testTag("speech_rate_slider"),
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
              text = "Voice Pitch: ${"%.2f".format(state.settings.speechPitch)}x",
              style = MaterialTheme.typography.labelMedium,
              color = accent,
            )
            Slider(
              value = state.settings.speechPitch,
              onValueChange = onSpeechPitchChange,
              valueRange = 0.6f..1.4f,
              colors = SliderDefaults.colors(thumbColor = accent, activeTrackColor = accent),
              modifier = Modifier.testTag("speech_pitch_slider"),
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Button(
                onClick = onTestVoice,
                colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = ObsidianBg),
                modifier = Modifier.weight(1f).height(48.dp).testTag("test_voice_button"),
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Test Voice")
              }

              OutlinedButton(
                onClick = onClearHistory,
                border = BorderStroke(1.dp, HudBorder),
                modifier = Modifier.weight(1f).height(48.dp).testTag("clear_history_button"),
              ) {
                Icon(
                  imageVector = Icons.Filled.DeleteSweep,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Clear Logs")
              }
            }
          }
        }
      }

      item(key = "creator_credit_card") {
        Surface(
          shape = MaterialTheme.shapes.large,
          color = HudElevated,
          border = BorderStroke(1.dp, accent.copy(alpha = 0.45f)),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Filled.SmartToy,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(22.dp),
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = stringResource(R.string.developer_credit_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Built as a futuristic voice-command & HUD assistant inspired by @pransh_official_coding.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
              onClick = onOpenCreatorChannel,
              colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = ObsidianBg),
              modifier = Modifier.fillMaxWidth().height(48.dp).testTag("open_creator_youtube_button"),
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(stringResource(R.string.developer_credit_channel))
            }
          }
        }
      }
    }
  }
}

@Composable
private fun PersonaRow(
  persona: AssistantPersona,
  selected: Boolean,
  onClick: () -> Unit,
) {
  Surface(
    onClick = onClick,
    shape = MaterialTheme.shapes.medium,
    color = if (selected) HudElevated else ObsidianBg.copy(alpha = 0.6f),
    border =
      BorderStroke(
        width = if (selected) 1.5.dp else 1.dp,
        color = if (selected) persona.accentColor else HudBorder,
      ),
    modifier = Modifier.fillMaxWidth().testTag("persona_option_${persona.name.lowercase()}"),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Surface(
        shape = CircleShape,
        color = persona.accentColor,
        modifier = Modifier.size(14.dp),
      ) {}
      Spacer(modifier = Modifier.width(12.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = persona.displayName,
          style = MaterialTheme.typography.titleMedium,
          color = Color.White,
        )
        Text(
          text = persona.subtitle,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      if (selected) {
        Icon(
          imageVector = Icons.Filled.CheckCircle,
          contentDescription = "Selected persona",
          tint = persona.accentColor,
        )
      }
    }
  }
}
