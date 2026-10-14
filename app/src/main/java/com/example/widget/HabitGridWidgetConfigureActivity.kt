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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Widget configuration: lets the user pick which habit a grid widget
 * instance displays. Launched by the launcher when the widget is added,
 * or by tapping an unconfigured grid widget.
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
            MaterialTheme(colorScheme = gridConfigColorScheme()) {
                HabitPickerScreen(
                    onHabitSelected = { habit -> confirmSelection(habit.id) }
                )
            }
        }
    }

    private fun confirmSelection(habitId: Long) {
        WidgetData.setGridHabitId(this, appWidgetId, habitId)
        HabitGridWidgetProvider.requestSingleUpdate(this, appWidgetId)
        val result = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, result)
        finish()
    }
}

private fun gridConfigColorScheme() = darkColorScheme(
    background = Color(0xFF0E1015),
    onBackground = Color(0xFFF8F9FA),
    surface = Color(0xFF16181F),
    onSurface = Color(0xFFF8F9FA),
    surfaceVariant = Color(0xFF272A35),
    onSurfaceVariant = Color(0xFF9AA0A6),
    outline = Color(0xFF272A35),
    primary = Color(0xFFA28BF7),
    onPrimary = Color.Black
)

@Composable
private fun HabitPickerScreen(onHabitSelected: (Habit) -> Unit) {
    val context = LocalContext.current
    var habits by remember { mutableStateOf<List<Habit>?>(null) }

    LaunchedEffect(Unit) {
        habits = withContext(Dispatchers.IO) {
            AppDatabase.getInstance(context).habitDao().getActiveHabitsOnce()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
    ) {
        Text(
            text = "Choose habit",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = "This grid widget will show the selected habit.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.size(16.dp))

        when (val loaded = habits) {
            null -> {
                Text(
                    text = "Loading…",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            else -> {
                if (loaded.isEmpty()) {
                    Text(
                        text = "No habits yet. Open the app to create one first.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(loaded, key = { it.id }) { habit ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable { onHabitSelected(habit) }
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
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
