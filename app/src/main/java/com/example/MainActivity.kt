package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.AuthUiState
import com.example.ui.NovaHudState
import com.example.ui.NovaViewModel
import com.example.ui.components.GoogleSignInRequiresFirebaseDialog
import com.example.ui.screens.AssistantScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CommandsScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.HudBorder
import com.example.ui.theme.HudElevated
import com.example.ui.theme.HudSurface
import com.example.ui.theme.NovaAITheme
import com.example.ui.theme.ObsidianBg

const val ROUTE_CORE = "core"
const val ROUTE_COMMANDS = "commands"
const val ROUTE_SYSTEM = "system"

private data class HudNavDestination(
  val route: String,
  val labelRes: Int,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  val testTag: String,
)

private val hudDestinations =
  listOf(
    HudNavDestination(
      route = ROUTE_CORE,
      labelRes = R.string.nav_core,
      selectedIcon = Icons.Filled.Bolt,
      unselectedIcon = Icons.Outlined.Bolt,
      testTag = "nav_tab_core",
    ),
    HudNavDestination(
      route = ROUTE_COMMANDS,
      labelRes = R.string.nav_commands,
      selectedIcon = Icons.Filled.Terminal,
      unselectedIcon = Icons.Outlined.Terminal,
      testTag = "nav_tab_commands",
    ),
    HudNavDestination(
      route = ROUTE_SYSTEM,
      labelRes = R.string.nav_system,
      selectedIcon = Icons.Filled.Person,
      unselectedIcon = Icons.Outlined.Person,
      testTag = "nav_tab_system",
    ),
  )

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val darkScrim = ObsidianBg.toArgb()
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.dark(darkScrim),
      navigationBarStyle = SystemBarStyle.dark(darkScrim),
    )
    setContent {
      val viewModel: NovaViewModel = viewModel()
      val state by viewModel.uiState.collectAsStateWithLifecycle()

      NovaAITheme(accentColor = state.settings.persona.accentColor) {
        when (state.authState) {
          AuthUiState.Loading -> {
            Box(
              modifier = Modifier.fillMaxSize().background(ObsidianBg),
              contentAlignment = Alignment.Center,
            ) {
              CircularProgressIndicator(color = state.settings.persona.accentColor)
            }
          }

          AuthUiState.SignedOut -> {
            AuthScreen(
              showGoogleFirebaseDialog = state.showGoogleFirebaseDialog,
              onGoogleSignInClick = viewModel::onGoogleSignInClicked,
              onDismissGoogleDialog = viewModel::dismissGoogleFirebaseDialog,
              onContinueLocal = viewModel::signInLocal,
              onOpenCreatorChannel = viewModel::openCreatorChannel,
            )
          }

          is AuthUiState.SignedIn -> {
            AuthenticatedHudApp(state = state, viewModel = viewModel)
          }
        }
      }
    }
  }
}

@Composable
private fun AuthenticatedHudApp(
  state: NovaHudState,
  viewModel: NovaViewModel,
) {
  val navController = rememberNavController()
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentDestination = navBackStackEntry?.destination
  val accent = state.settings.persona.accentColor

  if (state.showGoogleFirebaseDialog) {
    GoogleSignInRequiresFirebaseDialog(onDismiss = viewModel::dismissGoogleFirebaseDialog)
  }

  fun navigateToTab(route: String) {
    navController.navigate(route) {
      popUpTo(navController.graph.findStartDestination().id) { saveState = true }
      launchSingleTop = true
      restoreState = true
    }
  }

  BoxWithConstraints(modifier = Modifier.fillMaxSize().background(ObsidianBg)) {
    val useNavRail = maxWidth >= 600.dp

    if (useNavRail) {
      Row(
        modifier =
          Modifier.fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
      ) {
        NavigationRail(
          containerColor = HudSurface,
          contentColor = accent,
        ) {
          hudDestinations.forEach { dest ->
            val selected = currentDestination?.hierarchy?.any { it.route == dest.route } == true
            NavigationRailItem(
              selected = selected,
              onClick = { navigateToTab(dest.route) },
              icon = {
                Icon(
                  imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                  contentDescription = stringResource(dest.labelRes),
                )
              },
              label = { Text(stringResource(dest.labelRes)) },
              colors =
                NavigationRailItemDefaults.colors(
                  selectedIconColor = accent,
                  selectedTextColor = accent,
                  indicatorColor = HudElevated,
                  unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                  unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
              modifier = Modifier.testTag(dest.testTag),
            )
          }
        }

        Box(modifier = Modifier.weight(1f).imePadding()) {
          HudNavHost(navController = navController, state = state, viewModel = viewModel)
        }
      }
    } else {
      Scaffold(
        containerColor = ObsidianBg,
        contentWindowInsets = WindowInsets.statusBars,
        bottomBar = {
          NavigationBar(
            containerColor = HudSurface,
            tonalElevation = 4.dp,
            windowInsets = WindowInsets.navigationBars,
          ) {
            hudDestinations.forEach { dest ->
              val selected =
                currentDestination?.hierarchy?.any { it.route == dest.route } == true
              NavigationBarItem(
                selected = selected,
                onClick = { navigateToTab(dest.route) },
                icon = {
                  Icon(
                    imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                    contentDescription = stringResource(dest.labelRes),
                  )
                },
                label = {
                  Text(
                    text = stringResource(dest.labelRes),
                    style = MaterialTheme.typography.labelMedium,
                  )
                },
                colors =
                  NavigationBarItemDefaults.colors(
                    selectedIconColor = accent,
                    selectedTextColor = accent,
                    indicatorColor = HudElevated,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                  ),
                modifier = Modifier.testTag(dest.testTag),
              )
            }
          }
        },
      ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding).imePadding()) {
          HudNavHost(navController = navController, state = state, viewModel = viewModel)
        }
      }
    }
  }
}

@Composable
private fun HudNavHost(
  navController: androidx.navigation.NavHostController,
  state: NovaHudState,
  viewModel: NovaViewModel,
) {
  NavHost(
    navController = navController,
    startDestination = ROUTE_CORE,
    modifier = Modifier.fillMaxSize(),
  ) {
    composable(ROUTE_CORE) {
      AssistantScreen(
        state = state,
        onSendPrompt = viewModel::sendPrompt,
        onToggleListening = viewModel::toggleListening,
        onStopSpeaking = viewModel::stopSpeaking,
        onSpeakText = viewModel::speakText,
        onToggleAutoSpeak = viewModel::setAutoSpeak,
        onVoicePermissionDenied = {
          viewModel.showVoiceMessage(
            "Microphone permission denied. You can still type commands below."
          )
        },
        onDismissBanner = viewModel::dismissStatusBanner,
      )
    }

    composable(ROUTE_COMMANDS) {
      CommandsScreen(
        state = state,
        onRunMacro = { phrase ->
          viewModel.sendPrompt(phrase)
          navController.navigate(ROUTE_CORE) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
          }
        },
        onAddMacro = viewModel::addCustomMacro,
        onDeleteMacro = viewModel::deleteMacro,
        onRefreshTelemetry = viewModel::refreshTelemetry,
      )
    }

    composable(ROUTE_SYSTEM) {
      ProfileScreen(
        state = state,
        onGoogleSignInClick = viewModel::onGoogleSignInClicked,
        onSignOut = viewModel::signOut,
        onSelectPersona = viewModel::selectPersona,
        onToggleAutoSpeak = viewModel::setAutoSpeak,
        onSpeechRateChange = viewModel::setSpeechRate,
        onSpeechPitchChange = viewModel::setSpeechPitch,
        onTestVoice = {
          viewModel.speakText(
            "${state.settings.persona.displayName} voice systems calibrated and online."
          )
        },
        onClearHistory = viewModel::clearHistory,
        onOpenCreatorChannel = viewModel::openCreatorChannel,
      )
    }
  }
}
