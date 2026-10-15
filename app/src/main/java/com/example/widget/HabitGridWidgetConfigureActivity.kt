package com.example.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.AppDatabase
import com.example.data.model.Habit
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.HabitTrackerTheme
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Grid widget configuration: habit picker plus card appearance editor.
 * Runs both when the widget is added and when re-opened later
 * (launcher long-press → Settings, or the in-app widget list).
 */
class HabitGridWidgetConfigureActivity : ComponentActivity() {

    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            HabitTrackerTheme(themeMode = ThemeMode.SYSTEM) {
                GridConfigureScreen(
                    appWidgetId = appWidgetId,
                    onSave = { habitId, appearance ->
                        confirmSelection(habitId, appearance)
                    }
                )
            }
        }
    }

    private fun confirmSelection(habitId: Long, appearance: WidgetAppearance) {
        WidgetData.setGridHabitId(this, appWidgetId, habitId)
        WidgetAppearanceStore.save(this, appWidgetId, appearance)
        WidgetData.refreshAllWidgets(this)
        val result = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, result)
        finish()
    }
}

@Composable
private fun GridConfigureScreen(
    appWidgetId: Int,
    onSave: (habitId: Long, appearance: WidgetAppearance) -> Unit
) {
    val context = LocalContext.current
    var habits by remember { mutableStateOf<List<Habit>?>(null) }
    var selectedHabitId by remember { mutableLongStateOf(-1L) }
    var appearance by remember { mutableStateOf(WidgetAppearance()) }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(appWidgetId) {
        val dbHabits = withContext(Dispatchers.IO) {
            AppDatabase.getInstance(context).habitDao().getActiveHabitsOnce()
        }
        habits = dbHabits
        if (!loaded) {
            loaded = true
            val currentHabit = WidgetData.getGridHabitId(context, appWidgetId)
            selectedHabitId = if (dbHabits.any { it.id == currentHabit }) {
                currentHabit
            } else {
                -1L
            }
            appearance = WidgetAppearanceStore.load(context, appWidgetId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
    ) {
        Text(
            text = "Grid widget",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = "Choose a habit and style the card.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.size(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Habit",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            val loadedHabits = habits
            if (loadedHabits == null) {
                item {
                    Text(
                        text = "Loading…",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (loadedHabits.isEmpty()) {
                item {
                    Text(
                        text = "No habits yet. Open the app to create one first.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(loadedHabits, key = { it.id }) { habit ->
                    val isSelected = habit.id == selectedHabitId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isSelected) BrandPrimary.copy(alpha = 0.12f)
                                else MaterialTheme.colorScheme.surface
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) BrandPrimary
                                else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { selectedHabitId = habit.id }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color(habit.colorHex))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = habit.name,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (isSelected) FontWeight.Bold
                                else FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = BrandPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            item {
                WidgetAppearanceEditor(
                    appearance = appearance,
                    onChange = { appearance = it }
                )
            }

            item { Spacer(modifier = Modifier.height(4.dp)) }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = { onSave(selectedHabitId, appearance) },
            enabled = selectedHabitId != -1L,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
        ) {
            Text("Save widget", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}
