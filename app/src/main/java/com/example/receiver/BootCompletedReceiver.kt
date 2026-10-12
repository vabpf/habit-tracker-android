package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getInstance(context)
                    val habits = db.habitDao().getActiveHabitsOnce()

                    for (habit in habits) {
                        val reminders = db.habitReminderDao().getRemindersForHabit(habit.id).firstOrNull() ?: emptyList()
                        for (reminder in reminders) {
                            if (reminder.isEnabled) {
                                NotificationHelper.scheduleHabitReminder(
                                    context = context,
                                    reminderId = reminder.id,
                                    habitId = habit.id,
                                    hour = reminder.hour,
                                    minute = reminder.minute,
                                    daysOfWeek = reminder.daysOfWeek
                                )
                            }
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
}
