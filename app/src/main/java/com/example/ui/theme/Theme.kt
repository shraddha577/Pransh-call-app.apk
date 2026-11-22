package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val JarvisDarkColorScheme = darkColorScheme(
  primary = ArcCyan,
  onPrimary = ObsidianBg,
  primaryContainer = HudElevated,
  onPrimaryContainer = ArcCyan,
  secondary = PlasmaBlue,
  onSecondary = TextPrimary,
  secondaryContainer = HudCard,
  onSecondaryContainer = TextPrimary,
  tertiary = AmberAlert,
  onTertiary = ObsidianBg,
  background = ObsidianBg,
  onBackground = TextPrimary,
  surface = HudSurface,
  onSurface = TextPrimary,
  surfaceVariant = HudCard,
  onSurfaceVariant = TextSecondary,
  outline = HudBorder,
  error = CrimsonAccent,
  onError = TextPrimary,
)

private val JarvisShapes = Shapes(
  small = RoundedCornerShape(10.dp),
  medium = RoundedCornerShape(16.dp),
  large = RoundedCornerShape(24.dp),
)

@Composable
fun NovaAITheme(
  accentColor: androidx.compose.ui.graphics.Color = ArcCyan,
  content: @Composable () -> Unit,
) {
  val scheme = JarvisDarkColorScheme.copy(
    primary = accentColor,
    onPrimaryContainer = accentColor,
  )
  MaterialTheme(
    colorScheme = scheme,
    typography = JarvisTypography,
    shapes = JarvisShapes,
    content = content,
  )
}
