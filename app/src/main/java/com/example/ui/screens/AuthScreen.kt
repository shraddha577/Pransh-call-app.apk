package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.GoogleSignInRequiresFirebaseDialog
import com.example.ui.components.SampleAiNoticeBanner
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.HudBorder
import com.example.ui.theme.HudCard
import com.example.ui.theme.HudSurface
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.PlasmaBlue

@Composable
fun AuthScreen(
  showGoogleFirebaseDialog: Boolean,
  onGoogleSignInClick: () -> Unit,
  onDismissGoogleDialog: () -> Unit,
  onContinueLocal: (name: String, email: String) -> Unit,
  onOpenCreatorChannel: () -> Unit,
) {
  var name by rememberSaveable { mutableStateOf("") }
  var email by rememberSaveable { mutableStateOf("") }

  if (showGoogleFirebaseDialog) {
    GoogleSignInRequiresFirebaseDialog(onDismiss = onDismissGoogleDialog)
  }

  Box(
    modifier =
      Modifier.fillMaxSize()
        .background(
          Brush.verticalGradient(
            colors = listOf(ObsidianBg, HudSurface, ObsidianBg)
          )
        )
        .safeDrawingPadding(),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier =
        Modifier.widthIn(max = 520.dp)
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 24.dp, vertical = 20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Image(
        painter = painterResource(id = R.drawable.img_jarvis_core),
        contentDescription = "Nova AI neural core emblem",
        contentScale = ContentScale.Crop,
        modifier =
          Modifier.size(132.dp)
            .clip(CircleShape)
            .border(2.dp, ArcCyan.copy(alpha = 0.8f), CircleShape),
      )

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = stringResource(R.string.app_name).uppercase(),
        style = MaterialTheme.typography.displaySmall,
        color = MaterialTheme.colorScheme.onBackground,
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = stringResource(R.string.subtitle_neural_hud),
        style = MaterialTheme.typography.labelMedium,
        color = ArcCyan,
        textAlign = TextAlign.Center,
      )

      Spacer(modifier = Modifier.height(16.dp))

      SampleAiNoticeBanner()

      Spacer(modifier = Modifier.height(18.dp))

      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = HudCard,
        border = BorderStroke(1.dp, HudBorder),
        tonalElevation = 4.dp,
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Text(
            text = "Authenticate Session",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Link a Google Account (requires Firebase) or initialize a local device profile.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )

          Spacer(modifier = Modifier.height(16.dp))

          OutlinedButton(
            onClick = onGoogleSignInClick,
            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("google_sign_in_button"),
            border = BorderStroke(1.dp, ArcCyan.copy(alpha = 0.7f)),
            colors =
              ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
              ),
          ) {
            Icon(
              imageVector = Icons.Filled.AccountCircle,
              contentDescription = null,
              tint = ArcCyan,
              modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = stringResource(R.string.sign_in_with_google),
              style = MaterialTheme.typography.labelLarge,
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = HudBorder)
            Text(
              text = "LOCAL PROFILE",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = HudBorder)
          }

          Spacer(modifier = Modifier.height(14.dp))

          OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Commander Name") },
            placeholder = { Text("e.g. Pransh") },
            leadingIcon = {
              Icon(imageVector = Icons.Filled.Person, contentDescription = null)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            colors =
              OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ArcCyan,
                unfocusedBorderColor = HudBorder,
              ),
            modifier = Modifier.fillMaxWidth().testTag("local_name_input"),
          )

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email (Optional)") },
            placeholder = { Text("commander@local.device") },
            leadingIcon = {
              Icon(imageVector = Icons.Outlined.Email, contentDescription = null)
            },
            singleLine = true,
            keyboardOptions =
              KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done,
              ),
            keyboardActions =
              KeyboardActions(onDone = { onContinueLocal(name, email) }),
            colors =
              OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ArcCyan,
                unfocusedBorderColor = HudBorder,
              ),
            modifier = Modifier.fillMaxWidth().testTag("local_email_input"),
          )

          Spacer(modifier = Modifier.height(16.dp))

          Button(
            onClick = { onContinueLocal(name, email) },
            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("local_sign_in_button"),
            colors =
              ButtonDefaults.buttonColors(
                containerColor = PlasmaBlue,
                contentColor = MaterialTheme.colorScheme.onSecondary,
              ),
          ) {
            Icon(
              imageVector = Icons.Filled.Bolt,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = stringResource(R.string.continue_offline_profile),
              style = MaterialTheme.typography.labelLarge,
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      TextButton(
        onClick = onOpenCreatorChannel,
        modifier = Modifier.testTag("auth_creator_credit_button"),
      ) {
        Text(
          text = "${stringResource(R.string.developer_credit_title)} • YouTube",
          style = MaterialTheme.typography.labelMedium,
          color = ArcCyan,
        )
      }
    }
  }
}
