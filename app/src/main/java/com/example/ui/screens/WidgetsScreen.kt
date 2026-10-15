package com.example.ui.screens

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.AppDatabase
import com.example.ui.components.CircleIconButton
import com.example.ui.theme.BrandPrimary
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.HabitViewModel
import com.example.widget.HabitGridWidgetConfigureActivity
import com.example.widget.HabitGridWidgetProvider
import com.example.widget.HabitQuickWidgetConfigureActivity
import com.example.widget.HabitWidgetProvider
import com.example.widget.WidgetData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private data class PlacedWidget(
    val appWidgetId: Int,
    val title: String,
    val subtitle: String,
    val isGrid: Boolean
)

/**
 * In-app widget hub: every placed widget with an edit shortcut, plus
 * Add-to-home buttons for both widget types (system pin flow where the
 * launcher supports it).
 */
@Composable
fun WidgetsScreen(
    viewModel: HabitViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(AppScreen.SETTINGS)
    }

    val context = LocalContext.current
    var placed by remember { mutableStateOf<List<PlacedWidget>>(emptyList()) }

    LaunchedEffect(Unit) {
        placed = withContext(Dispatchers.IO) {
            loadPlacedWidgets(context)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircleIconButton(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        onClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                        testTag = "widgets_back_button"
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Widgets",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "Pin new ones or restyle placed ones.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            // Placed widgets
            item {
                SectionTitle("On your home screen")
            }
            if (placed.isEmpty()) {
                item {
                    Text(
                        text = "Nothing placed yet — add one below.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            } else {
                items(placed, key = { it.appWidgetId }) { widget ->
                    PlacedWidgetRow(
                        widget = widget,
                        onEdit = {
                            val target = if (widget.isGrid) {
                                HabitGridWidgetConfigureActivity::class.java
                            } else {
                                HabitQuickWidgetConfigureActivity::class.java
                            }
                            val intent = Intent(context, target).apply {
                                putExtra(
                                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                                    widget.appWidgetId
                                )
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }

            // Available widgets
            item {
                SectionTitle("Add widgets")
            }
            item {
                AvailableWidgetRow(
                    icon = Icons.Filled.List,
                    tint = BrandPrimary,
                    name = "Habit Quick Tracker",
                    description = "Today's habits with tap-to-complete.",
                    onAdd = {
                        pinWidget(
                            context = context,
                            provider = HabitWidgetProvider::class.java
                        )
                    }
                )
            }
            item {
                AvailableWidgetRow(
                    icon = Icons.Filled.GridView,
                    tint = Color(0xFF26D3EB),
                    name = "Habit Grid",
                    description = "Contribution grid for one habit you choose.",
                    onAdd = {
                        pinWidget(
                            context = context,
                            provider = HabitGridWidgetProvider::class.java
                        )
                    }
                )
            }

            item {
                Text(
                    text = "Tip: long-press any placed widget on your home screen, then tap Settings to change its habit, background, or opacity.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

private suspend fun loadPlacedWidgets(context: android.content.Context): List<PlacedWidget> {
    val manager = context.getSystemService(AppWidgetManager::class.java)
    val db = AppDatabase.getInstance(context)
    val habits = db.habitDao().getActiveHabitsOnce().associateBy { it.id }
    val out = mutableListOf<PlacedWidget>()
    manager.getAppWidgetIds(ComponentName(context, HabitWidgetProvider::class.java))
        .forEach { id ->
            out.add(
                PlacedWidget(
                    appWidgetId = id,
                    title = "Quick Tracker",
                    subtitle = "Today's habits",
                    isGrid = false
                )
            )
        }
    manager.getAppWidgetIds(ComponentName(context, HabitGridWidgetProvider::class.java))
        .forEach { id ->
            val habitId = WidgetData.getGridHabitId(context, id)
            out.add(
                PlacedWidget(
                    appWidgetId = id,
                    title = "Grid",
                    subtitle = habits[habitId]?.name ?: "No habit chosen",
                    isGrid = true
                )
            )
        }
    return out.sortedBy { it.appWidgetId }
}

private fun pinWidget(context: android.content.Context, provider: Class<*>) {
    val manager = context.getSystemService(AppWidgetManager::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
        manager.isRequestPinAppWidgetSupported
    ) {
        manager.requestPinAppWidget(
            ComponentName(context, provider),
            null,
            null
        )
    } else {
        Toast.makeText(
            context,
            "Pinning isn't supported — add it from your launcher's widget drawer instead.",
            Toast.LENGTH_LONG
        ).show()
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@Composable
private fun PlacedWidgetRow(
    widget: PlacedWidget,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onEdit() }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BrandPrimary.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (widget.isGrid) Icons.Filled.GridView else Icons.Filled.List,
                    contentDescription = null,
                    tint = BrandPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = widget.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = widget.subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Edit widget",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun AvailableWidgetRow(
    icon: ImageVector,
    tint: Color,
    name: String,
    description: String,
    onAdd: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tint.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onAdd,
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Add",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
