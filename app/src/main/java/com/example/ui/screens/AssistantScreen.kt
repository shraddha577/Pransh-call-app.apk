package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.local.ChatMessageEntity
import com.example.model.MessageRole
import com.example.model.VoiceStatus
import com.example.ui.NovaHudState
import com.example.ui.components.NeuralCoreOrb
import com.example.ui.components.SampleAiNoticeBanner
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.HudBorder
import com.example.ui.theme.HudCard
import com.example.ui.theme.HudElevated
import com.example.ui.theme.HudSurface
import com.example.ui.theme.ObsidianBg

@Composable
fun AssistantScreen(
  state: NovaHudState,
  onSendPrompt: (String) -> Unit,
  onToggleListening: () -> Unit,
  onStopSpeaking: () -> Unit,
  onSpeakText: (String) -> Unit,
  onToggleAutoSpeak: (Boolean) -> Unit,
  onVoicePermissionDenied: () -> Unit,
  onDismissBanner: () -> Unit,
) {
  val context = LocalContext.current
  val accent = state.settings.persona.accentColor
  var inputText by rememberSaveable { mutableStateOf("") }
  val listState = rememberLazyListState()

  val micPermissionLauncher =
    rememberLauncherForActivityResult(
      contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
      if (granted) {
        onToggleListening()
      } else {
        onVoicePermissionDenied()
      }
    }

  fun handleMicTap() {
    if (state.voiceStatus == VoiceStatus.SPEAKING) {
      onStopSpeaking()
      return
    }
    if (state.voiceStatus == VoiceStatus.LISTENING) {
      onToggleListening()
      return
    }
    val hasPermission =
      ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
        PackageManager.PERMISSION_GRANTED
    if (hasPermission) {
      onToggleListening()
    } else {
      micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }
  }

  LaunchedEffect(state.messages.size) {
    if (state.messages.isNotEmpty()) {
      listState.animateScrollToItem(state.messages.size)
    }
  }

  Box(
    modifier = Modifier.fillMaxSize().background(ObsidianBg),
    contentAlignment = Alignment.TopCenter,
  ) {
    Column(
      modifier = Modifier.widthIn(max = 840.dp).fillMaxSize(),
    ) {
      HudTopBar(
        personaName = state.settings.persona.displayName,
        accentColor = accent,
        batteryPercent = state.telemetry.batteryPercent,
        networkLabel = state.telemetry.networkLabel,
        autoSpeak = state.settings.autoSpeak,
        onToggleAutoSpeak = { onToggleAutoSpeak(!state.settings.autoSpeak) },
      )

      AnimatedVisibility(visible = state.statusBannerMessage != null) {
        state.statusBannerMessage?.let { msg ->
          Surface(
            modifier =
              Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
            shape = MaterialTheme.shapes.small,
            color = HudElevated,
            border = BorderStroke(1.dp, AmberAlert.copy(alpha = 0.6f)),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = msg,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
              )
              IconButton(
                onClick = onDismissBanner,
                modifier = Modifier.size(48.dp).testTag("dismiss_status_banner_button"),
              ) {
                Icon(
                  imageVector = Icons.Filled.Close,
                  contentDescription = "Dismiss message",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }
        }
      }

      LazyColumn(
        state = listState,
        modifier = Modifier.weight(1f).fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        item(key = "hud_core_header") {
          Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            SampleAiNoticeBanner()
            Spacer(modifier = Modifier.height(14.dp))
            NeuralCoreOrb(
              voiceStatus = state.voiceStatus,
              rmsLevel = state.rmsLevel,
              accentColor = accent,
              personaName = state.settings.persona.displayName,
              onClick = { handleMicTap() },
              orbSize = 148.dp,
            )
            if (state.partialTranscript.isNotBlank()) {
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "\"${state.partialTranscript}\"",
                style = MaterialTheme.typography.bodyMedium,
                color = EmeraldOnline,
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
          }
        }

        items(state.messages, key = { it.id }) { msg ->
          ChatMessageBubble(
            message = msg,
            personaName = state.settings.persona.displayName,
            accentColor = accent,
            onSpeak = { onSpeakText(msg.content) },
          )
        }
      }

      QuickChipRow(
        accentColor = accent,
        onSelectCommand = { onSendPrompt(it) },
      )

      CommandInputBar(
        value = inputText,
        onValueChange = { inputText = it },
        voiceStatus = state.voiceStatus,
        accentColor = accent,
        onSend = {
          if (inputText.isNotBlank()) {
            onSendPrompt(inputText)
            inputText = ""
          }
        },
        onMicClick = { handleMicTap() },
      )
    }
  }
}

@Composable
private fun HudTopBar(
  personaName: String,
  accentColor: Color,
  batteryPercent: Int,
  networkLabel: String,
  autoSpeak: Boolean,
  onToggleAutoSpeak: () -> Unit,
) {
  Surface(
    color = HudSurface,
    tonalElevation = 2.dp,
    border = BorderStroke(0.5.dp, HudBorder),
  ) {
    Row(
      modifier =
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier =
          Modifier.size(10.dp).background(accentColor, CircleShape)
      )
      Spacer(modifier = Modifier.width(10.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = personaName,
          style = MaterialTheme.typography.titleLarge,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Filled.BatteryChargingFull,
              contentDescription = null,
              tint = EmeraldOnline,
              modifier = Modifier.size(14.dp),
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = "$batteryPercent%",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Filled.Wifi,
              contentDescription = null,
              tint = accentColor,
              modifier = Modifier.size(14.dp),
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = networkLabel,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
      }

      IconButton(
        onClick = onToggleAutoSpeak,
        modifier = Modifier.size(48.dp).testTag("toggle_autospeak_button"),
      ) {
        Icon(
          imageVector =
            if (autoSpeak) Icons.AutoMirrored.Filled.VolumeUp
            else Icons.AutoMirrored.Filled.VolumeOff,
          contentDescription = if (autoSpeak) "Mute auto speech" else "Enable auto speech",
          tint = if (autoSpeak) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
private fun ChatMessageBubble(
  message: ChatMessageEntity,
  personaName: String,
  accentColor: Color,
  onSpeak: () -> Unit,
) {
  val role =
    MessageRole.entries.firstOrNull { it.name == message.role } ?: MessageRole.ASSISTANT
  val isUser = role == MessageRole.USER
  val isDeviceAction = role == MessageRole.DEVICE_ACTION

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
  ) {
    val containerColor =
      when {
        isUser -> HudElevated
        isDeviceAction -> HudCard
        else -> HudSurface
      }
    val borderColor =
      when {
        isUser -> accentColor.copy(alpha = 0.55f)
        isDeviceAction -> EmeraldOnline.copy(alpha = 0.65f)
        else -> HudBorder
      }

    Surface(
      modifier = Modifier.widthIn(max = 560.dp),
      shape =
        RoundedCornerShape(
          topStart = 18.dp,
          topEnd = 18.dp,
          bottomStart = if (isUser) 18.dp else 4.dp,
          bottomEnd = if (isUser) 4.dp else 18.dp,
        ),
      color = containerColor,
      border = BorderStroke(1.dp, borderColor),
    ) {
      Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
        if (!isUser) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector =
                  if (isDeviceAction) Icons.Filled.Terminal else Icons.Filled.Bolt,
                contentDescription = null,
                tint = if (isDeviceAction) EmeraldOnline else accentColor,
                modifier = Modifier.size(14.dp),
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text =
                  when {
                    isDeviceAction ->
                      "DEVICE ACTION • ${message.actionTag.ifBlank { "EXECUTED" }}"
                    message.isSampleAi -> "$personaName • SAMPLE AI"
                    else -> personaName
                  },
                style = MaterialTheme.typography.labelSmall,
                color = if (isDeviceAction) EmeraldOnline else accentColor,
              )
            }
            IconButton(
              onClick = onSpeak,
              modifier = Modifier.size(36.dp).testTag("speak_message_${message.id}"),
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = "Read response aloud",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
              )
            }
          }
          Spacer(modifier = Modifier.height(4.dp))
        }

        Text(
          text = message.content,
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurface,
        )
      }
    }
  }
}

@Composable
private fun QuickChipRow(
  accentColor: Color,
  onSelectCommand: (String) -> Unit,
) {
  val suggestions =
    listOf(
      "Battery status",
      "Open YouTube",
      "Search Jetpack Compose",
      "Open Pransh channel",
      "What time is it",
    )
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .horizontalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 6.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    suggestions.forEach { cmd ->
      AssistChip(
        onClick = { onSelectCommand(cmd) },
        label = {
          Text(
            text = cmd,
            style = MaterialTheme.typography.labelMedium,
          )
        },
        colors =
          AssistChipDefaults.assistChipColors(
            containerColor = HudCard,
            labelColor = accentColor,
          ),
        border = BorderStroke(1.dp, HudBorder),
        modifier = Modifier.testTag("quick_chip_${cmd.lowercase().replace(" ", "_")}"),
      )
    }
  }
}

@Composable
private fun CommandInputBar(
  value: String,
  onValueChange: (String) -> Unit,
  voiceStatus: VoiceStatus,
  accentColor: Color,
  onSend: () -> Unit,
  onMicClick: () -> Unit,
) {
  Surface(
    color = HudSurface,
    border = BorderStroke(0.5.dp, HudBorder),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      FilledIconButton(
        onClick = onMicClick,
        colors =
          IconButtonDefaults.filledIconButtonColors(
            containerColor =
              when (voiceStatus) {
                VoiceStatus.LISTENING -> EmeraldOnline
                VoiceStatus.SPEAKING -> AmberAlert
                else -> HudElevated
              },
            contentColor =
              when (voiceStatus) {
                VoiceStatus.LISTENING,
                VoiceStatus.SPEAKING -> ObsidianBg
                else -> accentColor
              },
          ),
        modifier = Modifier.size(48.dp).testTag("voice_mic_button"),
      ) {
        Icon(
          imageVector =
            when (voiceStatus) {
              VoiceStatus.LISTENING,
              VoiceStatus.SPEAKING -> Icons.Filled.Stop
              else -> Icons.Filled.Mic
            },
          contentDescription =
            when (voiceStatus) {
              VoiceStatus.LISTENING -> "Stop listening"
              VoiceStatus.SPEAKING -> "Stop speaking"
              else -> "Activate voice input"
            },
        )
      }

      OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
          Text(
            text = "Ask or command (e.g. Open YouTube)...",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
        keyboardActions = KeyboardActions(onSend = { onSend() }),
        colors =
          OutlinedTextFieldDefaults.colors(
            focusedBorderColor = accentColor,
            unfocusedBorderColor = HudBorder,
            focusedContainerColor = HudCard,
            unfocusedContainerColor = HudCard,
          ),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.weight(1f).testTag("command_text_input"),
      )

      FilledIconButton(
        onClick = onSend,
        enabled = value.isNotBlank(),
        colors =
          IconButtonDefaults.filledIconButtonColors(
            containerColor = accentColor,
            contentColor = ObsidianBg,
          ),
        modifier = Modifier.size(48.dp).testTag("send_command_button"),
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.Send,
          contentDescription = "Send command",
        )
      }
    }
  }
}
