package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * Single-habit contribution grid widget.
 * The displayed habit is chosen per widget instance via
 * [HabitGridWidgetConfigureActivity]. Tapping the grid (or the toggle
 * button) logs/unlogs today.
 */
class HabitGridWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_GRID_TOGGLE = "com.example.widget.ACTION_GRID_TOGGLE"
        const val EXTRA_APPWIDGET_ID = "extra_appwidget_id"

        const val GRID_COLUMNS = 16
        const val GRID_ROWS = 7

        fun updateAllGridWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, HabitGridWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds.isNotEmpty()) {
                val intent = Intent(context, HabitGridWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                }
                context.sendBroadcast(intent)
            }
        }

        /** Re-renders one widget instance (used after configuration). */
        fun requestSingleUpdate(context: Context, appWidgetId: Int) {
            val intent = Intent(context, HabitGridWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
            }
            context.sendBroadcast(intent)
        }
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        scope.launch {
            try {
                val db = AppDatabase.getInstance(context)
                val rendered = appWidgetIds.map { appWidgetId ->
                    appWidgetId to renderViews(context, db, appWidgetId)
                }
                withContext(Dispatchers.Main) {
                    rendered.forEach { (appWidgetId, views) ->
                        if (views != null) appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_GRID_TOGGLE) {
            val appWidgetId = intent.getIntExtra(
                EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                val pendingResult = goAsync()
                scope.launch {
                    try {
                        val habitId = WidgetData.getGridHabitId(context, appWidgetId)
                        if (habitId != -1L) {
                            WidgetData.toggleHabitDay(context, habitId)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        WidgetData.refreshAllWidgets(context)
                        pendingResult.finish()
                    }
                }
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        appWidgetIds.forEach {
            WidgetData.removeGridPrefs(context, it)
            WidgetAppearanceStore.remove(context, it)
        }
    }

    private suspend fun renderViews(
        context: Context,
        db: AppDatabase,
        appWidgetId: Int
    ): RemoteViews? {
        val views = RemoteViews(context.packageName, R.layout.widget_habit_grid)

        // Per-widget card background (hidden when disabled in settings).
        val cardBg = WidgetCardBackground.render(
            WidgetAppearanceStore.load(context, appWidgetId)
        )
        if (cardBg != null) {
            views.setViewVisibility(R.id.widget_card_bg, View.VISIBLE)
            views.setImageViewBitmap(R.id.widget_card_bg, cardBg)
        } else {
            views.setViewVisibility(R.id.widget_card_bg, View.GONE)
        }

        val appIntent = Intent(context, MainActivity::class.java)
        val appPendingIntent = PendingIntent.getActivity(
            context,
            100000 + appWidgetId,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val habitId = WidgetData.getGridHabitId(context, appWidgetId)
        val habit = if (habitId != -1L) db.habitDao().getHabitByIdOnce(habitId) else null

        if (habit == null) {
            // No habit chosen (or habit deleted): prompt to configure.
            views.setViewVisibility(R.id.widget_grid_header, View.GONE)
            views.setViewVisibility(R.id.widget_grid_image, View.GONE)
            views.setViewVisibility(R.id.widget_grid_hint, View.GONE)
            views.setViewVisibility(R.id.widget_grid_empty, View.VISIBLE)

            val configIntent = Intent(context, HabitGridWidgetConfigureActivity::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val configPendingIntent = PendingIntent.getActivity(
                context,
                200000 + appWidgetId,
                configIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_grid_empty, configPendingIntent)
            views.setOnClickPendingIntent(R.id.widget_grid_root, appPendingIntent)
            return views
        }

        views.setViewVisibility(R.id.widget_grid_header, View.VISIBLE)
        views.setViewVisibility(R.id.widget_grid_image, View.VISIBLE)
        views.setViewVisibility(R.id.widget_grid_hint, View.VISIBLE)
        views.setViewVisibility(R.id.widget_grid_empty, View.GONE)

        val logs = db.habitLogDao().getAllLogsOnce().filter { it.habitId == habit.id }
        val logMap = logs.associateBy { it.date }
        val today = LocalDate.now()
        val isDoneToday = WidgetData.isCompletedOn(habit, logMap, today)
        val streak = WidgetData.calculateStreak(habit, logs)

        views.setTextViewText(R.id.widget_grid_name, habit.name)
        views.setTextViewText(R.id.widget_grid_streak, "🔥 $streak")
        views.setImageViewResource(R.id.widget_grid_dot, R.drawable.widget_dot)
        views.setInt(R.id.widget_grid_dot, "setColorFilter", habit.colorHex.toInt())

        val toggleBg = if (isDoneToday) {
            R.drawable.widget_circle_checked
        } else {
            R.drawable.widget_circle_unchecked
        }
        views.setInt(R.id.widget_grid_toggle, "setBackgroundResource", toggleBg)

        // Contribution grid bitmap (square tiles, drawn off the UI thread).
        val bitmap = WidgetData.drawContributionGrid(
            habit = habit,
            logs = logs,
            columns = GRID_COLUMNS,
            rows = GRID_ROWS
        )
        views.setImageViewBitmap(R.id.widget_grid_image, bitmap)

        // Tap grid / toggle / header to log today; tap root opens the app.
        val toggleIntent = Intent(context, HabitGridWidgetProvider::class.java).apply {
            action = ACTION_GRID_TOGGLE
            putExtra(EXTRA_APPWIDGET_ID, appWidgetId)
        }
        val togglePendingIntent = PendingIntent.getBroadcast(
            context,
            300000 + appWidgetId,
            toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_grid_toggle, togglePendingIntent)
        views.setOnClickPendingIntent(R.id.widget_grid_image, togglePendingIntent)
        views.setOnClickPendingIntent(R.id.widget_grid_header, appPendingIntent)
        views.setOnClickPendingIntent(R.id.widget_grid_root, appPendingIntent)

        return views
    }
}
