package com.example.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.HabitTrackerTheme
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Quick-tracker configuration: card appearance only (the widget always
 * shows today's habits). Runs on add and on later reconfiguration.
 */
class HabitQuickWidgetConfigureActivity : ComponentActivity() {

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
                QuickConfigureScreen(
                    appWidgetId = appWidgetId,
                    onSave = { appearance -> confirmSelection(appearance) }
                )
            }
        }
    }

    private fun confirmSelection(appearance: WidgetAppearance) {
        WidgetAppearanceStore.save(this, appWidgetId, appearance)
        WidgetData.refreshAllWidgets(this)
        val result = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, result)
        finish()
    }
}

@Composable
private fun QuickConfigureScreen(
    appWidgetId: Int,
    onSave: (WidgetAppearance) -> Unit
) {
    val context = LocalContext.current
    var appearance by remember { mutableStateOf(WidgetAppearance()) }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(appWidgetId) {
        if (!loaded) {
            loaded = true
            appearance = withContext(Dispatchers.IO) {
                WidgetAppearanceStore.load(context, appWidgetId)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
    ) {
        Text(
            text = "Quick tracker",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = "Shows today's habits with tap-to-complete. Style the card below.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.size(20.dp))

        WidgetAppearanceEditor(
            appearance = appearance,
            onChange = { appearance = it },
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = { onSave(appearance) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
        ) {
            Text("Save widget", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}
