package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.HudCard

@Composable
fun SampleAiNoticeBanner(modifier: Modifier = Modifier) {
  Surface(
    modifier = modifier.fillMaxWidth().testTag("sample_ai_notice_banner"),
    shape = MaterialTheme.shapes.medium,
    color = HudCard.copy(alpha = 0.92f),
    border = BorderStroke(1.dp, AmberAlert.copy(alpha = 0.45f)),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        imageVector = Icons.Outlined.Info,
        contentDescription = "Sample AI notice",
        tint = AmberAlert,
        modifier = Modifier.size(18.dp),
      )
      Spacer(modifier = Modifier.width(10.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = stringResource(R.string.sample_ai_notice_title),
          style = MaterialTheme.typography.labelMedium,
          color = AmberAlert,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = stringResource(R.string.sample_ai_notice_body),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
fun GoogleSignInRequiresFirebaseDialog(
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = HudCard,
    icon = {
      Icon(
        imageVector = Icons.Outlined.CloudOff,
        contentDescription = "Firebase required",
        tint = AmberAlert,
      )
    },
    title = {
      Text(
        text = stringResource(R.string.google_signin_requires_firebase_title),
        style = MaterialTheme.typography.titleLarge,
      )
    },
    text = {
      Text(
        text = stringResource(R.string.google_signin_requires_firebase_body),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        modifier = Modifier.testTag("firebase_notice_ok_button"),
      ) {
        Text("Got It")
      }
    },
  )
}
