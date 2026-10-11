package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EditHabitScreen
import com.example.ui.screens.HabitDetailScreen
import com.example.ui.screens.NotesScreen
import com.example.ui.screens.RemindersScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.HabitTrackerTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.HabitViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: HabitViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()
            val selectedHabitId by viewModel.selectedHabitId.collectAsState()

            HabitTrackerTheme(themeMode = themeMode) {
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
                        }
                    }
                }
            }
        }
    }
}
