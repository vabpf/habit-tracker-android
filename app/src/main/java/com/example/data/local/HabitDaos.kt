package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Habit
import com.example.data.model.HabitLog
import com.example.data.model.HabitNote
import com.example.data.model.HabitReminder
import com.example.data.model.StreakFreezeState
import com.example.data.model.UnlockedBadge
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY displayOrder ASC, id ASC")
    fun getAllHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits ORDER BY isArchived ASC, displayOrder ASC, id ASC")
    fun getAllHabitsIncludingArchived(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE id = :id")
    fun getHabitById(id: Long): Flow<Habit?>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitByIdOnce(id: Long): Habit?

    @Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY displayOrder ASC, id ASC")
    suspend fun getActiveHabitsOnce(): List<Habit>

    @Query("SELECT * FROM habits ORDER BY displayOrder ASC, id ASC")
    suspend fun getAllHabitsOnce(): List<Habit>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabits(habits: List<Habit>): List<Long>

    @Update
    suspend fun updateHabit(habit: Habit)

    @Update
    suspend fun updateHabits(habits: List<Habit>)

    @Query("UPDATE habits SET isArchived = :isArchived WHERE id = :id")
    suspend fun updateArchiveStatus(id: Long, isArchived: Boolean)

    @Query("UPDATE habits SET displayOrder = :displayOrder WHERE id = :id")
    suspend fun updateHabitOrder(id: Long, displayOrder: Int)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("DELETE FROM habits")
    suspend fun deleteAllHabits()

    @Query("SELECT COUNT(*) FROM habits")
    suspend fun getHabitCount(): Int
}

@Dao
interface HabitLogDao {
    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY date DESC")
    fun getLogsForHabit(habitId: Long): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs")
    fun getAllLogs(): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun getLog(habitId: Long, date: String): HabitLog?

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND date = :date LIMIT 1")
    fun getLogFlow(habitId: Long, date: String): Flow<HabitLog?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLog(log: HabitLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<HabitLog>)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND date = :date")
    suspend fun deleteLog(habitId: Long, date: String)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId")
    suspend fun deleteAllLogsForHabit(habitId: Long)

    @Query("DELETE FROM habit_logs")
    suspend fun deleteAllLogs()
}

@Dao
interface HabitNoteDao {
    @Query("SELECT * FROM habit_notes WHERE habitId = :habitId ORDER BY date DESC, timestamp DESC")
    fun getNotesForHabit(habitId: Long): Flow<List<HabitNote>>

    @Query("SELECT * FROM habit_notes WHERE habitId = :habitId AND date = :date LIMIT 1")
    fun getNoteForDate(habitId: Long, date: String): Flow<HabitNote?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: HabitNote): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<HabitNote>)

    @Update
    suspend fun updateNote(note: HabitNote)

    @Query("DELETE FROM habit_notes WHERE id = :id")
    suspend fun deleteNote(id: Long)

    @Query("DELETE FROM habit_notes WHERE habitId = :habitId")
    suspend fun deleteAllNotesForHabit(habitId: Long)

    @Query("DELETE FROM habit_notes")
    suspend fun deleteAllNotes()
}

@Dao
interface HabitReminderDao {
    @Query("SELECT * FROM habit_reminders WHERE habitId = :habitId ORDER BY hour ASC, minute ASC")
    fun getRemindersForHabit(habitId: Long): Flow<List<HabitReminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: HabitReminder): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminders(reminders: List<HabitReminder>)

    @Update
    suspend fun updateReminder(reminder: HabitReminder)

    @Query("DELETE FROM habit_reminders WHERE id = :id")
    suspend fun deleteReminder(id: Long)

    @Query("DELETE FROM habit_reminders WHERE habitId = :habitId")
    suspend fun deleteAllRemindersForHabit(habitId: Long)

    @Query("DELETE FROM habit_reminders")
    suspend fun deleteAllReminders()
}

@Dao
interface StreakFreezeDao {
    @Query("SELECT * FROM streak_freezes WHERE id = 1 LIMIT 1")
    fun getStreakFreezeStateFlow(): Flow<StreakFreezeState?>

    @Query("SELECT * FROM streak_freezes WHERE id = 1 LIMIT 1")
    suspend fun getStreakFreezeStateOnce(): StreakFreezeState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStreakFreeze(state: StreakFreezeState): Long
}

@Dao
interface BadgeDao {
    @Query("SELECT * FROM unlocked_badges")
    fun getAllUnlockedBadgesFlow(): Flow<List<UnlockedBadge>>

    @Query("SELECT * FROM unlocked_badges")
    suspend fun getAllUnlockedBadgesOnce(): List<UnlockedBadge>

    @Query("SELECT * FROM unlocked_badges WHERE badgeId = :badgeId LIMIT 1")
    suspend fun getUnlockedBadge(badgeId: String): UnlockedBadge?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun unlockBadge(badge: UnlockedBadge): Long

    @Query("DELETE FROM unlocked_badges")
    suspend fun deleteAllUnlockedBadges()
}

