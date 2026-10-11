package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Habit
import com.example.data.model.HabitLog
import com.example.data.model.HabitNote
import com.example.data.model.HabitReminder
import com.example.data.model.HabitType
import com.example.data.repository.HabitRepository
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

enum class AppScreen {
    DASHBOARD,
    HABIT_DETAIL,
    ANALYTICS,
    NOTES,
    REMINDERS,
    EDIT_HABIT,
    SETTINGS
}

enum class DashboardViewMode {
    CLASSIC, // Grid cards
    COMPACT, // Routine at a glance (5 days)
    SPLIT    // Metric split view
}

data class HabitStats(
    val totalCompletions: Int,
    val completionRate: Int,
    val currentStreak: Int,
    val bestStreak: Int,
    val monthlyData: List<Pair<String, Int>>
)

class HabitViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HabitRepository

    init {
        val db = AppDatabase.getInstance(application)
        repository = HabitRepository(
            habitDao = db.habitDao(),
            habitLogDao = db.habitLogDao(),
            habitNoteDao = db.habitNoteDao(),
            habitReminderDao = db.habitReminderDao()
        )

        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    // Navigation State
    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _selectedHabitId = MutableStateFlow<Long?>(null)
    val selectedHabitId: StateFlow<Long?> = _selectedHabitId.asStateFlow()

    // Dashboard View Mode: Classic, Compact, Split
    private val _viewMode = MutableStateFlow(DashboardViewMode.CLASSIC)
    val viewMode: StateFlow<DashboardViewMode> = _viewMode.asStateFlow()

    // Selected Category Filter
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Selected Year for Analytics
    private val _selectedYear = MutableStateFlow(LocalDate.now().year)
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    // Theme Mode
    private val _themeMode = MutableStateFlow(ThemeMode.AMOLED)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    // Habit currently being edited (or null for creating new)
    private val _editingHabit = MutableStateFlow<Habit?>(null)
    val editingHabit: StateFlow<Habit?> = _editingHabit.asStateFlow()

    // All Habits
    val allHabits: StateFlow<List<Habit>> = repository.allHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Logs
    val allLogs: StateFlow<List<HabitLog>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Habits based on Category
    val filteredHabits: StateFlow<List<Habit>> = combine(allHabits, _selectedCategory) { habits, category ->
        if (category == "All") habits
        else habits.filter { it.category.equals(category, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun navigateTo(screen: AppScreen, habitId: Long? = null) {
        if (habitId != null) {
            _selectedHabitId.value = habitId
        }
        _currentScreen.value = screen
    }

    fun setViewMode(mode: DashboardViewMode) {
        _viewMode.value = mode
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }

    fun setYear(year: Int) {
        _selectedYear.value = year
    }

    fun startCreateHabit() {
        _editingHabit.value = null
        navigateTo(AppScreen.EDIT_HABIT)
    }

    fun startEditHabit(habit: Habit) {
        _editingHabit.value = habit
        navigateTo(AppScreen.EDIT_HABIT, habit.id)
    }

    // Toggle today or specified date
    fun toggleHabitDay(habit: Habit, date: LocalDate = LocalDate.now()) {
        val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        viewModelScope.launch {
            repository.toggleHabitDay(habit, dateStr)
        }
    }

    fun incrementHabitDay(habit: Habit, date: LocalDate = LocalDate.now(), step: Float = 1.0f) {
        val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        viewModelScope.launch {
            repository.incrementHabitDay(habit, dateStr, step)
        }
    }

    fun setHabitDayValue(habit: Habit, date: LocalDate, value: Float) {
        val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        viewModelScope.launch {
            repository.setHabitDayValue(habit, dateStr, value)
        }
    }

    fun saveHabit(habit: Habit) {
        viewModelScope.launch {
            val id = repository.saveHabit(habit)
            _selectedHabitId.value = id
            _currentScreen.value = AppScreen.DASHBOARD
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
            _currentScreen.value = AppScreen.DASHBOARD
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            _selectedHabitId.value = null
            _currentScreen.value = AppScreen.DASHBOARD
        }
    }

    fun reloadSampleData() {
        viewModelScope.launch {
            repository.reloadSampleData()
            _currentScreen.value = AppScreen.DASHBOARD
        }
    }

    // Notes
    fun getNotesForHabit(habitId: Long) = repository.getNotesForHabit(habitId)

    fun saveNote(habitId: Long, date: LocalDate, content: String, noteId: Long = 0L) {
        val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        viewModelScope.launch {
            repository.saveNote(
                HabitNote(
                    id = noteId,
                    habitId = habitId,
                    date = dateStr,
                    content = content
                )
            )
        }
    }

    fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            repository.deleteNote(noteId)
        }
    }

    // Reminders
    fun getRemindersForHabit(habitId: Long) = repository.getRemindersForHabit(habitId)

    fun saveReminder(reminder: HabitReminder) {
        viewModelScope.launch {
            repository.saveReminder(reminder)
        }
    }

    fun deleteReminder(reminderId: Long) {
        viewModelScope.launch {
            repository.deleteReminder(reminderId)
        }
    }

    // Streak & Stats Calculations
    fun calculateStats(habit: Habit, logs: List<HabitLog>, year: Int = LocalDate.now().year): HabitStats {
        val habitLogs = logs.filter { it.habitId == habit.id }
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val logMap = habitLogs.associateBy { it.date }

        val today = LocalDate.now()

        // Current Streak
        var currentStreak = 0
        var checkDate = today
        // If today is not logged yet, start checking from yesterday
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
            // Check yesterday
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
                checkDate = today // Streak broken
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

        // Best Streak (scan past 365 days)
        var bestStreak = 0
        var tempStreak = 0
        val startDate = today.minusDays(365)
        var iterDate = startDate
        while (!iterDate.isAfter(today)) {
            val dStr = iterDate.format(formatter)
            val log = logMap[dStr]
            val isDone = if (habit.habitType == HabitType.QUIT) {
                log == null || log.value <= 0f
            } else {
                log?.isCompleted == true
            }

            if (isDone) {
                tempStreak++
                if (tempStreak > bestStreak) bestStreak = tempStreak
            } else {
                tempStreak = 0
            }
            iterDate = iterDate.plusDays(1)
        }
        if (currentStreak > bestStreak) bestStreak = currentStreak

        // Total Completions in Year
        val yearLogs = habitLogs.filter {
            it.date.startsWith("$year-") && it.isCompleted
        }
        val totalCompletions = if (habit.habitType == HabitType.QUIT) {
            // Count clean days in year up to today
            val daysInYearSoFar = if (year == today.year) today.dayOfYear else 365
            val slips = habitLogs.count { it.date.startsWith("$year-") && it.value > 0f }
            (daysInYearSoFar - slips).coerceAtLeast(0)
        } else {
            yearLogs.size
        }

        // Completion Rate
        val daysElapsed = if (year == today.year) today.dayOfYear else 365
        val rate = if (daysElapsed > 0) ((totalCompletions.toFloat() / daysElapsed) * 100).toInt().coerceIn(0, 100) else 0

        // Monthly Data (Jan..Dec)
        val monthlyData = (1..12).map { month ->
            val monthEnum = java.time.Month.of(month)
            val monthLabel = monthEnum.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            val monthPrefix = String.format(Locale.US, "%04d-%02d", year, month)
            val count = habitLogs.count { it.date.startsWith(monthPrefix) && it.isCompleted }
            monthLabel to count
        }

        return HabitStats(
            totalCompletions = totalCompletions,
            completionRate = rate,
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            monthlyData = monthlyData
        )
    }
}
