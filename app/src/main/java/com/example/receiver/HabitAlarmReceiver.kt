package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.AppDatabase
import com.example.data.model.HabitType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HabitAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getLongExtra(NotificationHelper.EXTRA_HABIT_ID, -1L)
        val reminderId = intent.getLongExtra(NotificationHelper.EXTRA_REMINDER_ID, -1L)
        val hour = intent.getIntExtra("hour", 9)
        val minute = intent.getIntExtra("minute", 0)
        val daysOfWeek = intent.getStringExtra("daysOfWeek") ?: "1,2,3,4,5,6,7"

        if (habitId == -1L) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val habit = db.habitDao().getHabitByIdOnce(habitId)

                if (habit != null && !habit.isArchived) {
                    val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                    val existingLog = db.habitLogDao().getLog(habitId, todayStr)

                    val isAlreadyCompleted = if (habit.habitType == HabitType.QUIT) {
                        existingLog != null && existingLog.value > 0f // In QUIT habit, value > 0 is a slip
                    } else {
                        existingLog?.isCompleted == true
                    }

                    // Only send notification if user hasn't completed this habit today yet
                    if (!isAlreadyCompleted) {
                        // Calculate quick streak count
                        var streak = 0
                        var checkDate = LocalDate.now().minusDays(1)
                        while (true) {
                            val checkStr = checkDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                            val log = db.habitLogDao().getLog(habitId, checkStr)
                            if (log?.isCompleted == true) {
                                streak++
                                checkDate = checkDate.minusDays(1)
                            } else {
                                break
                            }
                        }

                        NotificationHelper.showHabitReminder(
                            context = context,
                            habitId = habit.id,
                            habitName = habit.name,
                            streakCount = streak,
                            category = habit.category
                        )
                    }

                    // Automatically reschedule the next recurring alarm
                    if (reminderId != -1L) {
                        NotificationHelper.scheduleHabitReminder(
                            context = context,
                            reminderId = reminderId,
                            habitId = habit.id,
                            hour = hour,
                            minute = minute,
                            daysOfWeek = daysOfWeek
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
