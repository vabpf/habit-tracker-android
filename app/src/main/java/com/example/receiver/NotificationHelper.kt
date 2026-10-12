package com.example.receiver

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

object NotificationHelper {

    const val CHANNEL_ID = "habit_reminders_channel"
    const val CHANNEL_NAME = "Habit Reminders"
    const val ACTION_COMPLETE_HABIT = "com.example.ACTION_COMPLETE_HABIT"
    const val EXTRA_HABIT_ID = "extra_habit_id"
    const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    const val EXTRA_REMINDER_ID = "extra_reminder_id"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                importance
            ).apply {
                description = "Daily notifications and quick actions for your habits"
                enableVibration(true)
                setShowBadge(true)
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showHabitReminder(
        context: Context,
        habitId: Long,
        habitName: String,
        streakCount: Int = 0,
        category: String = "Habits"
    ) {
        createNotificationChannel(context)

        val notificationId = habitId.toInt()

        // Intent to launch MainActivity and open habit
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_HABIT_ID, habitId)
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Quick-action intent to Complete habit directly from notification drawer
        val actionIntent = Intent(context, HabitActionReceiver::class.java).apply {
            action = ACTION_COMPLETE_HABIT
            putExtra(EXTRA_HABIT_ID, habitId)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val actionPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            actionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val subtitle = if (streakCount > 0) {
            "🔥 $streakCount-day streak! Check off today to keep it unbroken."
        } else {
            "Consistency starts today. Take 2 minutes to complete your $category habit!"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Time for: $habitName")
            .setContentText(subtitle)
            .setStyle(NotificationCompat.BigTextStyle().bigText(subtitle))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)
            .addAction(
                android.R.drawable.checkbox_on_background,
                "✓ Complete",
                actionPendingIntent
            )

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }

    fun showCompletionConfirmation(
        context: Context,
        notificationId: Int,
        habitName: String,
        newStreak: Int
    ) {
        createNotificationChannel(context)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.checkbox_on_background)
            .setContentTitle("✓ $habitName Completed!")
            .setContentText("Awesome! Streak is now $newStreak days 🔥 Keep it going!")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setTimeoutAfter(6000)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }

    fun cancelNotification(context: Context, notificationId: Int) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
    }

    fun scheduleHabitReminder(
        context: Context,
        reminderId: Long,
        habitId: Long,
        hour: Int,
        minute: Int,
        daysOfWeek: String = "1,2,3,4,5,6,7"
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, HabitAlarmReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_HABIT_ID, habitId)
            putExtra("hour", hour)
            putExtra("minute", minute)
            putExtra("daysOfWeek", daysOfWeek)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val targetMillis = calculateNextTriggerTime(hour, minute, daysOfWeek)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        targetMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        targetMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    targetMillis,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            // Graceful fallback for security / permission limits
            alarmManager.set(AlarmManager.RTC_WAKEUP, targetMillis, pendingIntent)
        }
    }

    fun cancelScheduledReminder(context: Context, reminderId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, HabitAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun calculateNextTriggerTime(hour: Int, minute: Int, daysOfWeek: String): Long {
        val now = LocalDateTime.now()
        val scheduledDays = daysOfWeek.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .map { DayOfWeek.of(it) }
            .toSet()

        var candidate = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)

        // If today's time has already passed or today is not a scheduled day, advance
        if (candidate.isBefore(now) || !scheduledDays.contains(candidate.dayOfWeek)) {
            // Find the next upcoming valid day
            var next = candidate.plusDays(1)
            for (i in 0..7) {
                if (scheduledDays.contains(next.dayOfWeek)) {
                    candidate = next
                    break
                }
                next = next.plusDays(1)
            }
        }

        return candidate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}
