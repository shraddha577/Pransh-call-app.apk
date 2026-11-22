package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.VoiceStatus
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.ObsidianBg

@Composable
fun NeuralCoreOrb(
  voiceStatus: VoiceStatus,
  rmsLevel: Float,
  accentColor: Color,
  personaName: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  orbSize: Dp = 156.dp,
) {
  val infinite = rememberInfiniteTransition(label = "core_orb_anim")
  val outerRotation by
    infinite.animateFloat(
      initialValue = 0f,
      targetValue = 360f,
      animationSpec =
        infiniteRepeatable(
          animation =
            tween(
              durationMillis = if (voiceStatus == VoiceStatus.IDLE) 16000 else 5500,
              easing = LinearEasing,
            )
        ),
      label = "outer_rotation",
    )
  val innerRotation by
    infinite.animateFloat(
      initialValue = 360f,
      targetValue = 0f,
      animationSpec =
        infiniteRepeatable(
          animation =
            tween(
              durationMillis = if (voiceStatus == VoiceStatus.IDLE) 11000 else 4200,
              easing = LinearEasing,
            )
        ),
      label = "inner_rotation",
    )
  val pulseBreath by
    infinite.animateFloat(
      initialValue = 0.96f,
      targetValue = 1.05f,
      animationSpec =
        infiniteRepeatable(
          animation = tween(1400, easing = FastOutSlowInEasing),
          repeatMode = RepeatMode.Reverse,
        ),
      label = "pulse_breath",
    )

  val rmsScale by
    animateFloatAsState(
      targetValue =
        when (voiceStatus) {
          VoiceStatus.LISTENING -> 1f + (rmsLevel * 0.14f)
          VoiceStatus.SPEAKING -> 1.06f
          VoiceStatus.THINKING -> 1.03f
          VoiceStatus.IDLE -> 1f
        },
      label = "rms_scale",
    )

  val activeRingColor =
    when (voiceStatus) {
      VoiceStatus.IDLE -> accentColor
      VoiceStatus.LISTENING -> EmeraldOnline
      VoiceStatus.THINKING -> AmberAlert
      VoiceStatus.SPEAKING -> accentColor
    }

  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Box(
      contentAlignment = Alignment.Center,
      modifier =
        Modifier.size(orbSize + 28.dp)
          .scale(if (voiceStatus == VoiceStatus.IDLE) pulseBreath else rmsScale)
          .clip(CircleShape)
          .clickable(onClick = onClick)
          .testTag("neural_core_orb_button"),
    ) {
      Canvas(modifier = Modifier.size(orbSize + 24.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val outerRadius = size.minDimension / 2f - 4.dp.toPx()

        drawCircle(
          brush =
            Brush.radialGradient(
              colors = listOf(activeRingColor.copy(alpha = 0.28f), Color.Transparent),
              center = center,
              radius = outerRadius,
            ),
          radius = outerRadius,
          center = center,
        )

        rotate(outerRotation, pivot = center) {
          drawArc(
            color = activeRingColor.copy(alpha = 0.85f),
            startAngle = 0f,
            sweepAngle = 240f,
            useCenter = false,
            topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
            size = Size(outerRadius * 2, outerRadius * 2),
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round),
          )
          drawArc(
            color = activeRingColor.copy(alpha = 0.35f),
            startAngle = 255f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
            size = Size(outerRadius * 2, outerRadius * 2),
            style =
              Stroke(
                width = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)),
              ),
          )
        }

        val innerRadius = outerRadius - 9.dp.toPx()
        rotate(innerRotation, pivot = center) {
          drawCircle(
            color = activeRingColor.copy(alpha = 0.45f),
            radius = innerRadius,
            center = center,
            style =
              Stroke(
                width = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 12f)),
              ),
          )
        }
      }

      Image(
        painter = painterResource(id = R.drawable.img_jarvis_core),
        contentDescription = "$personaName neural core orb. Tap to speak.",
        contentScale = ContentScale.Crop,
        modifier =
          Modifier.size(orbSize - 14.dp)
            .clip(CircleShape)
            .background(ObsidianBg)
            .border(1.5.dp, activeRingColor.copy(alpha = 0.7f), CircleShape),
      )
    }

    Spacer(modifier = Modifier.height(6.dp))

    val statusText =
      when (voiceStatus) {
        VoiceStatus.IDLE -> "$personaName STANDBY • TAP CORE TO SPEAK"
        VoiceStatus.LISTENING -> "LISTENING • SPEAK COMMAND NOW"
        VoiceStatus.THINKING -> "PROCESSING NEURAL REQUEST..."
        VoiceStatus.SPEAKING -> "SPEAKING • TAP TO INTERRUPT"
      }

    Text(
      text = statusText,
      style = MaterialTheme.typography.labelSmall,
      color = activeRingColor,
      modifier = Modifier.padding(horizontal = 12.dp),
    )
  }
}
