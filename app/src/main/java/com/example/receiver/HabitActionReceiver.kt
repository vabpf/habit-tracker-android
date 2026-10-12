package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.AppDatabase
import com.example.data.model.HabitLog
import com.example.data.model.HabitType
import com.example.data.model.UnlockedBadge
import com.example.widget.HabitWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HabitActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != NotificationHelper.ACTION_COMPLETE_HABIT) return

        val habitId = intent.getLongExtra(NotificationHelper.EXTRA_HABIT_ID, -1L)
        val notificationId = intent.getIntExtra(NotificationHelper.EXTRA_NOTIFICATION_ID, habitId.toInt())

        if (habitId == -1L) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val habit = db.habitDao().getHabitByIdOnce(habitId)

                if (habit != null) {
                    val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

                    // Insert or update log for today as completed
                    val log = HabitLog(
                        habitId = habit.id,
                        date = todayStr,
                        value = habit.targetValue,
                        isCompleted = true,
                        timestamp = System.currentTimeMillis()
                    )
                    db.habitLogDao().insertOrUpdateLog(log)

                    // Calculate current streak
                    var currentStreak = 1
                    var checkDate = LocalDate.now().minusDays(1)
                    while (true) {
                        val checkStr = checkDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                        val pastLog = db.habitLogDao().getLog(habitId, checkStr)
                        if (pastLog?.isCompleted == true) {
                            currentStreak++
                            checkDate = checkDate.minusDays(1)
                        } else {
                            break
                        }
                    }

                    // Check and award badges
                    db.badgeDao().unlockBadge(
                        UnlockedBadge("first_step", System.currentTimeMillis())
                    )
                    if (currentStreak >= 3) {
                        db.badgeDao().unlockBadge(
                            UnlockedBadge("streak_3", System.currentTimeMillis())
                        )
                    }
                    if (currentStreak >= 7) {
                        db.badgeDao().unlockBadge(
                            UnlockedBadge("streak_7", System.currentTimeMillis())
                        )
                    }

                    // Show confirmation notification
                    NotificationHelper.showCompletionConfirmation(
                        context = context,
                        notificationId = notificationId,
                        habitName = habit.name,
                        newStreak = currentStreak
                    )

                    // Refresh any installed Home Screen widgets
                    HabitWidgetProvider.updateAllWidgets(context)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
