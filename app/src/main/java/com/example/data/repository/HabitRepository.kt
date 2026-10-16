package com.example.data.repository

import com.example.data.local.HabitDao
import com.example.data.local.HabitLogDao
import com.example.data.local.HabitNoteDao
import com.example.data.local.HabitReminderDao
import com.example.data.model.Habit
import com.example.data.model.HabitLog
import com.example.data.model.HabitNote
import com.example.data.model.HabitReminder
import com.example.data.model.HabitType
import kotlinx.coroutines.flow.Flow

import com.example.data.local.BadgeDao
import com.example.data.local.StreakFreezeDao
import com.example.data.model.StreakFreezeState
import com.example.data.model.UnlockedBadge

class HabitRepository(
    private val habitDao: HabitDao,
    private val habitLogDao: HabitLogDao,
    private val habitNoteDao: HabitNoteDao,
    private val habitReminderDao: HabitReminderDao,
    private val streakFreezeDao: StreakFreezeDao,
    private val badgeDao: BadgeDao
) {
    val allHabits: Flow<List<Habit>> = habitDao.getAllHabits()
    val allHabitsIncludingArchived: Flow<List<Habit>> = habitDao.getAllHabitsIncludingArchived()
    val allLogs: Flow<List<HabitLog>> = habitLogDao.getAllLogs()
    val streakFreezeState: Flow<StreakFreezeState?> = streakFreezeDao.getStreakFreezeStateFlow()
    val unlockedBadges: Flow<List<UnlockedBadge>> = badgeDao.getAllUnlockedBadgesFlow()

    fun getHabit(id: Long): Flow<Habit?> = habitDao.getHabitById(id)
    suspend fun getHabitByIdOnce(id: Long): Habit? = habitDao.getHabitByIdOnce(id)
    suspend fun getAllHabitsOnce(): List<Habit> = habitDao.getAllHabitsOnce()
    suspend fun getActiveHabitsOnce(): List<Habit> = habitDao.getActiveHabitsOnce()
    fun getLogsForHabit(habitId: Long): Flow<List<HabitLog>> = habitLogDao.getLogsForHabit(habitId)
    fun getNotesForHabit(habitId: Long): Flow<List<HabitNote>> = habitNoteDao.getNotesForHabit(habitId)
    fun getRemindersForHabit(habitId: Long): Flow<List<HabitReminder>> = habitReminderDao.getRemindersForHabit(habitId)

    suspend fun getStreakFreezeStateOnce(): StreakFreezeState {
        val existing = streakFreezeDao.getStreakFreezeStateOnce()
        if (existing != null) return existing
        val defaultState = StreakFreezeState(id = 1, availableTokens = 2, maxTokens = 3, autoFreezeEnabled = true)
        streakFreezeDao.insertOrUpdateStreakFreeze(defaultState)
        return defaultState
    }

    suspend fun updateStreakFreeze(state: StreakFreezeState) {
        streakFreezeDao.insertOrUpdateStreakFreeze(state)
    }

    suspend fun useStreakFreeze(date: String): Boolean {
        val current = getStreakFreezeStateOnce()
        if (current.availableTokens <= 0) return false
        val existingDates = current.getFrozenDatesList().toMutableList()
        if (!existingDates.contains(date)) {
            existingDates.add(date)
        }
        val updated = current.copy(
            availableTokens = (current.availableTokens - 1).coerceAtLeast(0),
            frozenDates = existingDates.joinToString(","),
            totalFreezesUsed = current.totalFreezesUsed + 1
        )
        streakFreezeDao.insertOrUpdateStreakFreeze(updated)
        unlockBadge("freeze_shield")
        return true
    }

    suspend fun awardStreakFreezeToken(): Boolean {
        val current = getStreakFreezeStateOnce()
        if (current.availableTokens >= current.maxTokens) return false
        val updated = current.copy(availableTokens = current.availableTokens + 1)
        streakFreezeDao.insertOrUpdateStreakFreeze(updated)
        return true
    }

    suspend fun setAutoFreeze(enabled: Boolean) {
        val current = getStreakFreezeStateOnce()
        streakFreezeDao.insertOrUpdateStreakFreeze(current.copy(autoFreezeEnabled = enabled))
    }

    suspend fun unlockBadge(badgeId: String): Boolean {
        val existing = badgeDao.getUnlockedBadge(badgeId)
        if (existing != null) return false
        badgeDao.unlockBadge(UnlockedBadge(badgeId = badgeId, unlockedAt = System.currentTimeMillis()))
        return true
    }

    suspend fun isHabitCompletedOnDate(habitId: Long, date: String): Boolean {
        val log = habitLogDao.getLog(habitId, date)
        val habit = habitDao.getHabitByIdOnce(habitId) ?: return false
        return if (habit.habitType == HabitType.QUIT) {
            log == null || log.value <= 0f
        } else {
            log?.isCompleted == true
        }
    }

    suspend fun saveHabit(habit: Habit): Long {
        return if (habit.id == 0L) {
            habitDao.insertHabit(habit)
        } else {
            habitDao.updateHabit(habit)
            habit.id
        }
    }

    suspend fun setHabitArchived(habitId: Long, isArchived: Boolean) {
        habitDao.updateArchiveStatus(habitId, isArchived)
    }

    suspend fun reorderHabits(habits: List<Habit>) {
        val updated = habits.mapIndexed { index, habit ->
            habit.copy(displayOrder = index)
        }
        habitDao.updateHabits(updated)
    }

    suspend fun deleteHabit(habit: Habit) {
        habitDao.deleteHabit(habit)
        habitLogDao.deleteAllLogsForHabit(habit.id)
        habitNoteDao.deleteAllNotesForHabit(habit.id)
        habitReminderDao.deleteAllRemindersForHabit(habit.id)
    }

    suspend fun toggleHabitDay(habit: Habit, date: String): Boolean {
        val existing = habitLogDao.getLog(habit.id, date)
        return if (existing != null && existing.isCompleted) {
            // Delete or mark uncompleted
            habitLogDao.deleteLog(habit.id, date)
            false
        } else {
            val log = HabitLog(
                habitId = habit.id,
                date = date,
                value = habit.targetValue,
                isCompleted = true
            )
            habitLogDao.insertOrUpdateLog(log)
            true
        }
    }

    suspend fun incrementHabitDay(habit: Habit, date: String, step: Float = 1.0f): Float {
        val existing = habitLogDao.getLog(habit.id, date)
        val currentVal = existing?.value ?: 0f
        val newVal = currentVal + step
        val isCompleted = newVal >= habit.targetValue
        val log = HabitLog(
            id = existing?.id ?: 0L,
            habitId = habit.id,
            date = date,
            value = newVal,
            isCompleted = isCompleted
        )
        habitLogDao.insertOrUpdateLog(log)
        return newVal
    }

    suspend fun setHabitDayValue(habit: Habit, date: String, value: Float) {
        if (value <= 0f) {
            habitLogDao.deleteLog(habit.id, date)
        } else {
            val existing = habitLogDao.getLog(habit.id, date)
            val log = HabitLog(
                id = existing?.id ?: 0L,
                habitId = habit.id,
                date = date,
                value = value,
                isCompleted = value >= habit.targetValue
            )
            habitLogDao.insertOrUpdateLog(log)
        }
    }

    suspend fun saveNote(note: HabitNote): Long {
        return if (note.id == 0L) {
            habitNoteDao.insertNote(note)
        } else {
            habitNoteDao.updateNote(note)
            note.id
        }
    }

    suspend fun deleteNote(noteId: Long) {
        habitNoteDao.deleteNote(noteId)
    }

    suspend fun saveReminder(reminder: HabitReminder): Long {
        return if (reminder.id == 0L) {
            habitReminderDao.insertReminder(reminder)
        } else {
            habitReminderDao.updateReminder(reminder)
            reminder.id
        }
    }

    suspend fun deleteReminder(reminderId: Long) {
        habitReminderDao.deleteReminder(reminderId)
    }

    suspend fun clearAllData() {
        habitDao.deleteAllHabits()
        habitLogDao.deleteAllLogs()
        habitNoteDao.deleteAllNotes()
        habitReminderDao.deleteAllReminders()
        badgeDao.deleteAllUnlockedBadges()
        streakFreezeDao.insertOrUpdateStreakFreeze(
            StreakFreezeState(id = 1, availableTokens = 2, maxTokens = 3, autoFreezeEnabled = true)
        )
    }

    /** Serializes the whole database to a JSON backup string. */
    suspend fun exportBackup(): String {
        return HabitBackup.export(
            habits = habitDao.getAllHabitsOnce(),
            logs = habitLogDao.getAllLogsOnce(),
            notes = habitNoteDao.getAllNotesOnce(),
            reminders = habitReminderDao.getAllRemindersOnce(),
            badges = badgeDao.getAllUnlockedBadgesOnce(),
            freezeState = streakFreezeDao.getStreakFreezeStateOnce()
        )
    }

    /**
     * Replaces the whole database with a JSON backup.
     * @return human-readable summary. Throws on invalid content.
     */
    suspend fun importBackup(json: String): String {
        val payload = HabitBackup.parse(json)
        clearAllData()
        if (payload.habits.isNotEmpty()) habitDao.insertHabits(payload.habits)
        if (payload.logs.isNotEmpty()) habitLogDao.insertLogs(payload.logs)
        if (payload.notes.isNotEmpty()) habitNoteDao.insertNotes(payload.notes)
        if (payload.reminders.isNotEmpty()) habitReminderDao.insertReminders(payload.reminders)
        payload.badges.forEach { badgeDao.unlockBadge(it) }
        payload.freezeState?.let { streakFreezeDao.insertOrUpdateStreakFreeze(it) }
        return "${payload.habits.size} habits, ${payload.logs.size} logs restored"
    }

}
