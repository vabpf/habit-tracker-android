package com.example

import android.app.Activity
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EditHabitScreen
import com.example.ui.screens.HabitDetailScreen
import com.example.ui.screens.NotesScreen
import com.example.ui.screens.RemindersScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.HabitTrackerTheme
import com.example.ui.theme.ThemeMode
import com.example.ui.util.LocalBackdrop
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.HabitViewModel
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Keep system bars fully transparent: without this the OS draws dark
        // contrast scrims over the status/nav bars (visible as black bands).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            window.isStatusBarContrastEnforced = false
        }
        setContent {
            val viewModel: HabitViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()
            val selectedHabitId by viewModel.selectedHabitId.collectAsState()

            HabitTrackerTheme(themeMode = themeMode) {
                // Match status/nav icon style to the selected theme so icons
                // stay readable over the transparent system bars.
                EdgeToEdgeBarIcons(themeMode = themeMode)
                // Shared backdrop sampled by floating glass chrome (e.g. the
                // Dashboard view-mode switcher). Screens apply `layerBackdrop`
                // to their scrollable content; glass samplers must sit OUTSIDE
                // that node as later siblings.
                val contentBackdrop = rememberLayerBackdrop()
                CompositionLocalProvider(LocalBackdrop provides contentBackdrop) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
                        when (screen) {
                            AppScreen.DASHBOARD -> {
                                DashboardScreen(viewModel = viewModel)
                            }
                            AppScreen.HABIT_DETAIL -> {
                                val habitId = selectedHabitId ?: 1L
                                HabitDetailScreen(habitId = habitId, viewModel = viewModel)
                            }
                            AppScreen.ANALYTICS -> {
                                AnalyticsScreen(viewModel = viewModel)
                            }
                            AppScreen.NOTES -> {
                                val habitId = selectedHabitId ?: 1L
                                NotesScreen(habitId = habitId, viewModel = viewModel)
                            }
                            AppScreen.REMINDERS -> {
                                val habitId = selectedHabitId ?: 1L
                                RemindersScreen(habitId = habitId, viewModel = viewModel)
                            }
                            AppScreen.EDIT_HABIT -> {
                                EditHabitScreen(viewModel = viewModel)
                            }
                            AppScreen.SETTINGS -> {
                                SettingsScreen(viewModel = viewModel)
                            }
                            AppScreen.BADGES -> {
                                com.example.ui.screens.BadgesScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
                }
            }
        }
    }
}

/**
 * Keeps status/navigation bar icons readable over the transparent
 * edge-to-edge bars for the currently selected [ThemeMode].
 * Dark themes (incl. AMOLED) need light icons; LIGHT needs dark icons.
 */
@Composable
private fun EdgeToEdgeBarIcons(themeMode: ThemeMode) {
    val view = LocalView.current
    val systemDark = isSystemInDarkTheme()
    val useDarkIcons = when (themeMode) {
        ThemeMode.LIGHT -> true
        ThemeMode.SYSTEM -> !systemDark
        ThemeMode.DARK, ThemeMode.AMOLED -> false
    }
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        WindowInsetsControllerCompat(window, view).apply {
            isAppearanceLightStatusBars = useDarkIcons
            isAppearanceLightNavigationBars = useDarkIcons
        }
    }
}
