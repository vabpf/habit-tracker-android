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
import com.example.data.model.TrackingType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.random.Random

class HabitRepository(
    private val habitDao: HabitDao,
    private val habitLogDao: HabitLogDao,
    private val habitNoteDao: HabitNoteDao,
    private val habitReminderDao: HabitReminderDao
) {
    val allHabits: Flow<List<Habit>> = habitDao.getAllHabits()
    val allHabitsIncludingArchived: Flow<List<Habit>> = habitDao.getAllHabitsIncludingArchived()
    val allLogs: Flow<List<HabitLog>> = habitLogDao.getAllLogs()

    fun getHabit(id: Long): Flow<Habit?> = habitDao.getHabitById(id)
    fun getLogsForHabit(habitId: Long): Flow<List<HabitLog>> = habitLogDao.getLogsForHabit(habitId)
    fun getNotesForHabit(habitId: Long): Flow<List<HabitNote>> = habitNoteDao.getNotesForHabit(habitId)
    fun getRemindersForHabit(habitId: Long): Flow<List<HabitReminder>> = habitReminderDao.getRemindersForHabit(habitId)

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
    }

    suspend fun reloadSampleData() {
        clearAllData()
        seedDataInternal()
    }

    suspend fun seedInitialDataIfEmpty() {
        if (habitDao.getHabitCount() > 0) return
        seedDataInternal()
    }

    private suspend fun seedDataInternal() {

        val initialHabits = listOf(
            Habit(
                name = "Morning Run",
                description = "Start the day with a run",
                iconName = "run",
                colorHex = 0xFF8AE034,
                habitType = HabitType.BUILD,
                trackingType = TrackingType.YES_NO,
                targetValue = 1f,
                unit = "km",
                displayOrder = 0,
                category = "Fitness"
            ),
            Habit(
                name = "Drink Water",
                description = "Stay hydrated throughout the day",
                iconName = "water",
                colorHex = 0xFF26D3EB,
                habitType = HabitType.BUILD,
                trackingType = TrackingType.MEASURE,
                targetValue = 2.0f,
                unit = "L",
                displayOrder = 1,
                category = "Health"
            ),
            Habit(
                name = "Push-ups",
                description = "Build strength, one rep at a time",
                iconName = "fitness",
                colorHex = 0xFFF7BA32,
                habitType = HabitType.BUILD,
                trackingType = TrackingType.COUNT,
                targetValue = 50f,
                unit = "reps",
                displayOrder = 2,
                category = "Fitness"
            ),
            Habit(
                name = "Read 20 pages",
                description = "Make time for a good book",
                iconName = "book",
                colorHex = 0xFFA28BF7,
                habitType = HabitType.BUILD,
                trackingType = TrackingType.COUNT,
                targetValue = 20f,
                unit = "pages",
                displayOrder = 3,
                category = "Mind"
            ),
            Habit(
                name = "Meditate",
                description = "A quiet moment for yourself",
                iconName = "meditate",
                colorHex = 0xFF3BD9A5,
                habitType = HabitType.BUILD,
                trackingType = TrackingType.YES_NO,
                targetValue = 1f,
                unit = "session",
                displayOrder = 4,
                category = "Mind"
            ),
            Habit(
                name = "Journal",
                description = "Clear your mind on paper",
                iconName = "journal",
                colorHex = 0xFFF06595,
                habitType = HabitType.BUILD,
                trackingType = TrackingType.YES_NO,
                targetValue = 1f,
                unit = "entry",
                displayOrder = 5,
                category = "Mind"
            ),
            Habit(
                name = "No Sugar",
                description = "Choose a life with less sugar",
                iconName = "nosugar",
                colorHex = 0xFFFA5252,
                habitType = HabitType.QUIT,
                trackingType = TrackingType.YES_NO,
                targetValue = 1f,
                unit = "clean day",
                displayOrder = 6,
                category = "Health"
            ),
            Habit(
                name = "Guitar",
                description = "Practice chords and songs",
                iconName = "guitar",
                colorHex = 0xFF339AF0,
                habitType = HabitType.BUILD,
                trackingType = TrackingType.YES_NO,
                targetValue = 1f,
                unit = "session",
                displayOrder = 7,
                category = "Hobbies"
            ),
            Habit(
                name = "Evening Walk",
                description = "Unwind before bed",
                iconName = "walk",
                colorHex = 0xFFFD7E14,
                habitType = HabitType.BUILD,
                trackingType = TrackingType.YES_NO,
                targetValue = 1f,
                unit = "walk",
                displayOrder = 8,
                category = "Health"
            ),
            Habit(
                name = "Stretching",
                description = "Stay flexible and injury-free",
                iconName = "stretch",
                colorHex = 0xFF20C997,
                habitType = HabitType.BUILD,
                trackingType = TrackingType.YES_NO,
                targetValue = 1f,
                unit = "session",
                displayOrder = 9,
                category = "Fitness"
            ),
            Habit(
                name = "Sleep 8 hours",
                description = "Rest deeply for recovery",
                iconName = "sleep",
                colorHex = 0xFF748FFC,
                habitType = HabitType.BUILD,
                trackingType = TrackingType.MEASURE,
                targetValue = 8f,
                unit = "hrs",
                displayOrder = 10,
                category = "Health"
            ),
            Habit(
                name = "Cycling",
                description = "Weekend trails & cardio",
                iconName = "bike",
                colorHex = 0xFFFCC419,
                habitType = HabitType.BUILD,
                trackingType = TrackingType.YES_NO,
                targetValue = 1f,
                unit = "ride",
                displayOrder = 11,
                category = "Fitness"
            ),
            Habit(
                name = "Learn Spanish",
                description = "15 minutes of vocabulary",
                iconName = "language",
                colorHex = 0xFFD0BFFF,
                habitType = HabitType.BUILD,
                trackingType = TrackingType.YES_NO,
                targetValue = 1f,
                unit = "lesson",
                displayOrder = 12,
                category = "Mind"
            ),
            Habit(
                name = "Floss",
                description = "Daily dental care",
                iconName = "floss",
                colorHex = 0xFF4DABF7,
                habitType = HabitType.BUILD,
                trackingType = TrackingType.YES_NO,
                targetValue = 1f,
                unit = "session",
                displayOrder = 13,
                category = "Health"
            )
        )

        val insertedIds = habitDao.insertHabits(initialHabits)

        // Seed logs for the past 140 days
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val today = LocalDate.now()
        val allSeededLogs = mutableListOf<HabitLog>()

        // Generate high-consistency logs with small gaps
        val random = Random(42)
        insertedIds.forEachIndexed { index, habitId ->
            val habit = initialHabits[index]
            val completionRate = when (index) {
                0 -> 0.78 // Morning Run
                1 -> 0.85 // Drink Water
                2 -> 0.75 // Push-ups
                3 -> 0.82 // Read 20 pages
                4 -> 0.90 // Meditate
                5 -> 0.70 // Journal
                6 -> 0.92 // No Sugar
                else -> 0.65 + random.nextDouble() * 0.25
            }

            for (dayOffset in 0..140) {
                val date = today.minusDays(dayOffset.toLong())
                val dateStr = date.format(formatter)
                val isCompleted = random.nextDouble() < completionRate

                if (isCompleted || dayOffset < 3) { // Ensure recent 3 days are solid
                    val value = when (habit.trackingType) {
                        TrackingType.YES_NO -> 1.0f
                        TrackingType.COUNT -> habit.targetValue
                        TrackingType.MEASURE -> habit.targetValue
                    }
                    allSeededLogs.add(
                        HabitLog(
                            habitId = habitId,
                            date = dateStr,
                            value = value,
                            isCompleted = true
                        )
                    )
                }
            }
        }
        habitLogDao.insertLogs(allSeededLogs)

        // Seed realistic notes matching screenshots for Water & Meditate
        val waterId = insertedIds.getOrNull(1) ?: 2L
        val notes = listOf(
            HabitNote(
                habitId = waterId,
                date = today.format(formatter),
                content = "Morning headache — pushing water early today."
            ),
            HabitNote(
                habitId = waterId,
                date = today.minusDays(27).format(formatter),
                content = "Two big bottles at my desk did the trick."
            ),
            HabitNote(
                habitId = waterId,
                date = today.minusDays(30).format(formatter),
                content = "Long hike, drank way more than usual."
            ),
            HabitNote(
                habitId = waterId,
                date = today.minusDays(34).format(formatter),
                content = "Forgot my bottle at home, catch-up evening."
            ),
            HabitNote(
                habitId = waterId,
                date = today.minusDays(39).format(formatter),
                content = "Tea counts, right? Counting it anyway."
            ),
            HabitNote(
                habitId = waterId,
                date = today.minusDays(46).format(formatter),
                content = "Travel day, airport water is a scam but I paid up."
            ),
            HabitNote(
                habitId = waterId,
                date = today.minusDays(56).format(formatter),
                content = "New rule: one glass before every coffee."
            )
        )
        habitNoteDao.insertNotes(notes)

        // Seed reminders matching screenshot 7
        val reminders = listOf(
            HabitReminder(
                habitId = waterId,
                hour = 7,
                minute = 0,
                daysOfWeek = "1,2,3,4,5"
            ),
            HabitReminder(
                habitId = waterId,
                hour = 21,
                minute = 30,
                daysOfWeek = "1,2,3,4,5,6,7"
            )
        )
        habitReminderDao.insertReminders(reminders)
    }
}
