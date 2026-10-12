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
import com.example.data.model.Habit
import com.example.data.model.HabitLog
import com.example.data.model.HabitType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HabitWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_TOGGLE_HABIT = "com.example.widget.ACTION_TOGGLE_HABIT"
        const val EXTRA_HABIT_ID = "extra_habit_id"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, HabitWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds.isNotEmpty()) {
                val intent = Intent(context, HabitWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                }
                context.sendBroadcast(intent)
            }
        }
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        scope.launch {
            try {
                val db = AppDatabase.getInstance(context)
                val habits = db.habitDao().getActiveHabitsOnce()
                val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                val allLogs = db.habitLogDao().getAllLogsOnce()
                val completedHabitIds = allLogs
                    .filter { it.date == today && it.isCompleted }
                    .map { it.habitId }
                    .toSet()

                // Calculate streaks for each habit
                val habitStreaks = habits.associate { habit ->
                    val habitLogs = allLogs.filter { it.habitId == habit.id }
                    habit.id to calculateStreak(habit, habitLogs)
                }

                withContext(Dispatchers.Main) {
                    for (appWidgetId in appWidgetIds) {
                        renderWidget(context, appWidgetManager, appWidgetId, habits, completedHabitIds, habitStreaks)
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
        if (intent.action == ACTION_TOGGLE_HABIT) {
            val habitId = intent.getLongExtra(EXTRA_HABIT_ID, -1L)
            if (habitId != -1L) {
                val pendingResult = goAsync()
                scope.launch {
                    try {
                        val db = AppDatabase.getInstance(context)
                        val habit = db.habitDao().getHabitByIdOnce(habitId)
                        if (habit != null) {
                            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                            val existingLog = db.habitLogDao().getLog(habitId, today)
                            if (existingLog == null) {
                                db.habitLogDao().insertOrUpdateLog(
                                    HabitLog(
                                        habitId = habitId,
                                        date = today,
                                        value = habit.targetValue,
                                        isCompleted = true,
                                        timestamp = System.currentTimeMillis()
                                    )
                                )
                            } else {
                                val newCompleted = !existingLog.isCompleted
                                val newValue = if (newCompleted) habit.targetValue else 0f
                                db.habitLogDao().insertOrUpdateLog(
                                    existingLog.copy(
                                        isCompleted = newCompleted,
                                        value = newValue,
                                        timestamp = System.currentTimeMillis()
                                    )
                                )
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        updateAllWidgets(context)
                        pendingResult.finish()
                    }
                }
            }
        }
    }

    private fun renderWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        habits: List<Habit>,
        completedHabitIds: Set<Long>,
        habitStreaks: Map<Long, Int>
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_habit_tracker)

        // Launch MainActivity when clicking the title/root
        val appIntent = Intent(context, MainActivity::class.java)
        val appPendingIntent = PendingIntent.getActivity(
            context,
            0,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_title, appPendingIntent)

        val totalHabits = habits.size
        val completedCount = habits.count { completedHabitIds.contains(it.id) }
        val progressPercent = if (totalHabits > 0) (completedCount * 100) / totalHabits else 0

        views.setTextViewText(R.id.widget_progress_text, "$completedCount/$totalHabits")
        views.setProgressBar(R.id.widget_progress_bar, 100, progressPercent, false)

        val slotViews = listOf(
            Triple(R.id.habit_slot_1, R.id.habit_name_1, Pair(R.id.habit_btn_1, R.id.habit_streak_1)),
            Triple(R.id.habit_slot_2, R.id.habit_name_2, Pair(R.id.habit_btn_2, R.id.habit_streak_2)),
            Triple(R.id.habit_slot_3, R.id.habit_name_3, Pair(R.id.habit_btn_3, R.id.habit_streak_3))
        )

        if (habits.isEmpty()) {
            views.setViewVisibility(R.id.widget_empty_text, View.VISIBLE)
            slotViews.forEach { views.setViewVisibility(it.first, View.GONE) }
        } else {
            views.setViewVisibility(R.id.widget_empty_text, View.GONE)
            for (i in 0 until 3) {
                val (slotLayoutId, nameViewId, btnAndStreak) = slotViews[i]
                val (btnViewId, streakViewId) = btnAndStreak

                if (i < habits.size) {
                    val habit = habits[i]
                    val isDone = completedHabitIds.contains(habit.id)
                    val streak = habitStreaks[habit.id] ?: 0

                    views.setViewVisibility(slotLayoutId, View.VISIBLE)
                    views.setTextViewText(nameViewId, habit.name)
                    views.setTextViewText(streakViewId, "🔥 $streak")

                    // Set button background depending on done state
                    val bgRes = if (isDone) R.drawable.widget_circle_checked else R.drawable.widget_circle_unchecked
                    views.setInt(btnViewId, "setBackgroundResource", bgRes)

                    // Intent for toggle action
                    val toggleIntent = Intent(context, HabitWidgetProvider::class.java).apply {
                        action = ACTION_TOGGLE_HABIT
                        putExtra(EXTRA_HABIT_ID, habit.id)
                    }
                    val togglePendingIntent = PendingIntent.getBroadcast(
                        context,
                        (habit.id * 100 + i).toInt(),
                        toggleIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                    )
                    views.setOnClickPendingIntent(btnViewId, togglePendingIntent)
                    views.setOnClickPendingIntent(slotLayoutId, appPendingIntent)
                } else {
                    views.setViewVisibility(slotLayoutId, View.GONE)
                }
            }
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun calculateStreak(habit: Habit, logs: List<HabitLog>): Int {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val logMap = logs.associateBy { it.date }
        val today = LocalDate.now()

        var currentStreak = 0
        var checkDate = today
        val todayStr = today.format(formatter)
        val todayLog = logMap[todayStr]
        val todayCompleted = if (habit.habitType == HabitType.QUIT) {
            todayLog == null || todayLog.value <= 0f
        } else {
            todayLog?.isCompleted == true
        }

        if (todayCompleted) {
            currentStreak++
            checkDate = today.minusDays(1)
        } else {
            val yesterday = today.minusDays(1)
            val yesterdayStr = yesterday.format(formatter)
            val yesterdayLog = logMap[yesterdayStr]
            val yesterdayCompleted = if (habit.habitType == HabitType.QUIT) {
                yesterdayLog == null || yesterdayLog.value <= 0f
            } else {
                yesterdayLog?.isCompleted == true
            }
            if (yesterdayCompleted) {
                checkDate = yesterday
            } else {
                checkDate = today
            }
        }

        while (true) {
            val dateStr = checkDate.format(formatter)
            val log = logMap[dateStr]
            val isDone = if (habit.habitType == HabitType.QUIT) {
                log == null || log.value <= 0f
            } else {
                log?.isCompleted == true
            }

            if (isDone) {
                if (checkDate != today || !todayCompleted) {
                    currentStreak++
                }
                checkDate = checkDate.minusDays(1)
            } else {
                break
            }
        }
        return currentStreak
    }
}
