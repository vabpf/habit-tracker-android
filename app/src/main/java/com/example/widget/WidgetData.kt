package com.example.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.RectF
import com.example.data.local.AppDatabase
import com.example.data.model.Habit
import com.example.data.model.HabitLog
import com.example.data.model.HabitType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Shared logic for the home-screen widgets (quick tracker + grid).
 *
 * RemoteViews cannot draw Compose canvases, so the contribution grid is
 * rendered to a [Bitmap] here ([drawContributionGrid]) and set on an
 * ImageView via `setImageViewBitmap`.
 */
object WidgetData {

    private const val PREFS_GRID = "habit_grid_widget_prefs"
    private const val KEY_HABIT_PREFIX = "grid_habit_id_"

    /** Returns the habit chosen for a grid widget instance, or -1 if none. */
    fun getGridHabitId(context: Context, appWidgetId: Int): Long {
        return context.getSharedPreferences(PREFS_GRID, Context.MODE_PRIVATE)
            .getLong(KEY_HABIT_PREFIX + appWidgetId, -1L)
    }

    fun setGridHabitId(context: Context, appWidgetId: Int, habitId: Long) {
        context.getSharedPreferences(PREFS_GRID, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_HABIT_PREFIX + appWidgetId, habitId)
            .apply()
    }

    fun removeGridPrefs(context: Context, appWidgetId: Int) {
        context.getSharedPreferences(PREFS_GRID, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_HABIT_PREFIX + appWidgetId)
            .apply()
    }

    /** Flips today's completion for a habit (YES_NO toggle semantics). */
    suspend fun toggleHabitDay(context: Context, habitId: Long) {
        val db = AppDatabase.getInstance(context)
        val habit = db.habitDao().getHabitByIdOnce(habitId) ?: return
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

    /** Refreshes every widget instance (quick tracker + all grids). */
    fun refreshAllWidgets(context: Context) {
        HabitWidgetProvider.updateAllWidgets(context)
        HabitGridWidgetProvider.updateAllGridWidgets(context)
    }

    fun isCompletedOn(
        habit: Habit,
        logMap: Map<String, HabitLog>,
        date: LocalDate
    ): Boolean {
        val log = logMap[date.format(DateTimeFormatter.ISO_LOCAL_DATE)]
        return if (habit.habitType == HabitType.QUIT) {
            log == null || log.value <= 0f
        } else {
            log?.isCompleted == true
        }
    }

    fun calculateStreak(habit: Habit, logs: List<HabitLog>): Int {
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

    /**
     * Renders a GitHub-style contribution grid to a bitmap with square tiles.
     * Background is transparent so the widget card shows through.
     * Today is marked with a white ring.
     */
    fun drawContributionGrid(
        habit: Habit,
        logs: List<HabitLog>,
        columns: Int = 16,
        rows: Int = 7,
        tilePx: Int = 56,
        gapPx: Int = 10
    ): Bitmap {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val logMap = logs.associate { it.date to it.value }
        val today = LocalDate.now()

        // End on Sunday so columns align, same as the in-app grid.
        val endDate = today.plusDays((7 - today.dayOfWeek.value).toLong())
        val startDate = endDate.minusDays((columns * rows - 1).toLong())

        val width = columns * tilePx + (columns - 1) * gapPx
        val height = rows * tilePx + (rows - 1) * gapPx
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val baseColor = habit.colorHex.toInt()
        val inactiveColor = AndroidColor.parseColor("#1A1C24")
        val radius = tilePx * 0.28f

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = (tilePx * 0.09f).coerceAtLeast(2f)
            color = AndroidColor.WHITE
            alpha = 220
        }

        for (col in 0 until columns) {
            for (row in 0 until rows) {
                val cellDate = startDate.plusDays((col * rows + row).toLong())
                val left = (col * (tilePx + gapPx)).toFloat()
                val top = (row * (tilePx + gapPx)).toFloat()
                val rect = RectF(left, top, left + tilePx, top + tilePx)
                val isFuture = cellDate.isAfter(today)

                bgPaint.color = inactiveColor
                // Translucent so the frosted card shows through instead of
                // punching opaque black holes in the glass.
                bgPaint.alpha = if (isFuture) 50 else 100
                canvas.drawRoundRect(rect, radius, radius, bgPaint)

                val loggedValue = logMap[cellDate.format(formatter)]
                val isFilled = if (isFuture) {
                    false
                } else if (habit.habitType == HabitType.QUIT) {
                    loggedValue == null || loggedValue <= 0f
                } else {
                    loggedValue != null && loggedValue >= (habit.targetValue * 0.3f)
                }

                if (isFilled) {
                    val ratio = if (habit.habitType == HabitType.QUIT) {
                        1f
                    } else {
                        ((loggedValue ?: 0f) / habit.targetValue).coerceIn(0.4f, 1f)
                    }
                    fillPaint.color = baseColor
                    fillPaint.alpha = (255 * ratio).toInt()
                    canvas.drawRoundRect(rect, radius, radius, fillPaint)
                }

                if (cellDate == today) {
                    val inset = ringPaint.strokeWidth / 2f
                    canvas.drawRoundRect(
                        RectF(
                            rect.left + inset,
                            rect.top + inset,
                            rect.right - inset,
                            rect.bottom - inset
                        ),
                        radius,
                        radius,
                        ringPaint
                    )
                }
            }
        }
        return bitmap
    }
}
