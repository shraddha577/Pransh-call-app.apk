package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.CommandMacroEntity
import com.example.ui.NovaHudState
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.HudBorder
import com.example.ui.theme.HudCard
import com.example.ui.theme.HudElevated
import com.example.ui.theme.ObsidianBg

@Composable
fun CommandsScreen(
  state: NovaHudState,
  onRunMacro: (String) -> Unit,
  onAddMacro: (title: String, triggerPhrase: String, description: String, category: String) -> Unit,
  onDeleteMacro: (Long) -> Unit,
  onRefreshTelemetry: () -> Unit,
) {
  val accent = state.settings.persona.accentColor
  var showAddDialog by rememberSaveable { mutableStateOf(false) }

  if (showAddDialog) {
    AddMacroDialog(
      accentColor = accent,
      onDismiss = { showAddDialog = false },
      onConfirm = { title, phrase, desc ->
        onAddMacro(title, phrase, desc, "Custom")
        showAddDialog = false
      },
    )
  }

  Box(
    modifier = Modifier.fillMaxSize().background(ObsidianBg),
    contentAlignment = Alignment.TopCenter,
  ) {
    LazyColumn(
      modifier = Modifier.widthIn(max = 840.dp).fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      item(key = "telemetry_banner") {
        Surface(
          shape = MaterialTheme.shapes.large,
          color = HudCard,
          border = BorderStroke(1.dp, HudBorder),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Box {
            Image(
              painter = painterResource(id = R.drawable.img_hud_banner),
              contentDescription = "HUD telemetry horizon artwork",
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxWidth().height(128.dp).clip(MaterialTheme.shapes.large),
            )
            Box(
              modifier =
                Modifier.fillMaxWidth()
                  .height(128.dp)
                  .background(
                    Brush.verticalGradient(
                      colors = listOf(ObsidianBg.copy(alpha = 0.25f), HudCard.copy(alpha = 0.94f))
                    )
                  )
            )
            Row(
              modifier = Modifier.fillMaxWidth().padding(16.dp).align(Alignment.BottomStart),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "COMMAND DECK",
                  style = MaterialTheme.typography.headlineMedium,
                  color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                  text = "${state.telemetry.localDate} • ${state.telemetry.localTime}",
                  style = MaterialTheme.typography.labelMedium,
                  color = accent,
                )
              }
              IconButton(
                onClick = onRefreshTelemetry,
                modifier = Modifier.size(48.dp).testTag("refresh_telemetry_button"),
              ) {
                Icon(
                  imageVector = Icons.Filled.Refresh,
                  contentDescription = "Refresh device telemetry",
                  tint = accent,
                )
              }
            }
          }
        }
      }

      item(key = "telemetry_cards") {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          TelemetryTile(
            icon = Icons.Filled.BatteryChargingFull,
            label = "POWER",
            value = "${state.telemetry.batteryPercent}%",
            subValue = if (state.telemetry.isCharging) "Charging" else "Discharging",
            tint = EmeraldOnline,
            modifier = Modifier.weight(1f),
          )
          TelemetryTile(
            icon = Icons.Filled.Wifi,
            label = "LINK",
            value = state.telemetry.networkLabel,
            subValue = "Active Net",
            tint = accent,
            modifier = Modifier.weight(1f),
          )
          TelemetryTile(
            icon = Icons.Filled.Devices,
            label = "SYSTEM",
            value = "Android ${state.telemetry.androidVersion}",
            subValue = state.telemetry.deviceModel,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f),
          )
        }
      }

      item(key = "macros_header") {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Column {
            Text(
              text = "VOICE & TAP MACROS",
              style = MaterialTheme.typography.titleLarge,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = "Tap any macro to execute directly on your device",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          Button(
            onClick = { showAddDialog = true },
            colors =
              ButtonDefaults.buttonColors(
                containerColor = accent,
                contentColor = ObsidianBg,
              ),
            modifier = Modifier.height(48.dp).testTag("add_macro_button"),
          ) {
            Icon(
              imageVector = Icons.Filled.Add,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("New", style = MaterialTheme.typography.labelLarge)
          }
        }
      }

      items(state.macros, key = { it.id }) { macro ->
        MacroCard(
          macro = macro,
          accentColor = accent,
          onRun = { onRunMacro(macro.triggerPhrase) },
          onDelete = { onDeleteMacro(macro.id) },
        )
      }
    }
  }
}

@Composable
private fun TelemetryTile(
  icon: ImageVector,
  label: String,
  value: String,
  subValue: String,
  tint: Color,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    shape = MaterialTheme.shapes.medium,
    color = HudCard,
    border = BorderStroke(1.dp, HudBorder),
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = tint,
          modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = label,
          style = MaterialTheme.typography.labelSmall,
          color = tint,
        )
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Text(
        text = subValue,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun MacroCard(
  macro: CommandMacroEntity,
  accentColor: Color,
  onRun: () -> Unit,
  onDelete: () -> Unit,
) {
  Surface(
    onClick = onRun,
    shape = MaterialTheme.shapes.medium,
    color = HudCard,
    border = BorderStroke(1.dp, HudBorder),
    modifier = Modifier.fillMaxWidth().testTag("macro_card_${macro.id}"),
  ) {
    Row(
      modifier = Modifier.padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Surface(
        shape = MaterialTheme.shapes.small,
        color = HudElevated,
        modifier = Modifier.size(44.dp),
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = "Execute ${macro.title}",
            tint = accentColor,
          )
        }
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = macro.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = macro.category.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = accentColor,
          )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "\"${macro.triggerPhrase}\" — ${macro.description}",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
      }

      IconButton(
        onClick = onDelete,
        modifier = Modifier.size(48.dp).testTag("delete_macro_${macro.id}"),
      ) {
        Icon(
          imageVector = Icons.Filled.DeleteOutline,
          contentDescription = "Delete ${macro.title}",
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
private fun AddMacroDialog(
  accentColor: Color,
  onDismiss: () -> Unit,
  onConfirm: (title: String, triggerPhrase: String, description: String) -> Unit,
) {
  var title by rememberSaveable { mutableStateOf("") }
  var phrase by rememberSaveable { mutableStateOf("") }
  var description by rememberSaveable { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = HudCard,
    title = {
      Text("Create Command Macro", style = MaterialTheme.typography.titleLarge)
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Macro Name") },
          placeholder = { Text("e.g. Open Camera") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("macro_title_input"),
        )
        OutlinedTextField(
          value = phrase,
          onValueChange = { phrase = it },
          label = { Text("Command / Prompt") },
          placeholder = { Text("e.g. Open camera") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("macro_phrase_input"),
        )
        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          label = { Text("Description (Optional)") },
          placeholder = { Text("Launches device camera") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("macro_desc_input"),
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirm(title, phrase, description) },
        enabled = title.isNotBlank() && phrase.isNotBlank(),
        colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = ObsidianBg),
        modifier = Modifier.testTag("save_macro_button"),
      ) {
        Text("Save Macro")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    },
  )
}
