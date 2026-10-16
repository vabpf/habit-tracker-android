package com.example.data.repository

import com.example.data.model.Habit
import com.example.data.model.HabitLog
import com.example.data.model.HabitNote
import com.example.data.model.HabitReminder
import com.example.data.model.StreakFreezeState
import com.example.data.model.UnlockedBadge
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * Full-database backup/restore serialized as JSON (habits, logs, notes,
 * reminders, badges, streak-freeze state). Uses Moshi reflection so the
 * Room entities need no annotations.
 */
object HabitBackup {

    const val BACKUP_VERSION = 1
    const val MIME_TYPE = "application/json"

    data class BackupPayload(
        val version: Int = BACKUP_VERSION,
        val habits: List<Habit> = emptyList(),
        val logs: List<HabitLog> = emptyList(),
        val notes: List<HabitNote> = emptyList(),
        val reminders: List<HabitReminder> = emptyList(),
        val badges: List<UnlockedBadge> = emptyList(),
        val freezeState: StreakFreezeState? = null
    )

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    private val adapter by lazy {
        moshi.adapter(BackupPayload::class.java).indent("  ")
    }

    fun export(
        habits: List<Habit>,
        logs: List<HabitLog>,
        notes: List<HabitNote>,
        reminders: List<HabitReminder>,
        badges: List<UnlockedBadge>,
        freezeState: StreakFreezeState?
    ): String {
        return adapter.toJson(
            BackupPayload(
                habits = habits,
                logs = logs,
                notes = notes,
                reminders = reminders,
                badges = badges,
                freezeState = freezeState
            )
        )
    }

    /** Parses and validates a backup file. Throws on invalid content. */
    fun parse(json: String): BackupPayload {
        val payload = adapter.fromJson(json)
            ?: throw IllegalArgumentException("Empty backup file")
        if (payload.version != BACKUP_VERSION) {
            throw IllegalArgumentException(
                "Unsupported backup version ${payload.version} (expected $BACKUP_VERSION)"
            )
        }
        return payload
    }
}
