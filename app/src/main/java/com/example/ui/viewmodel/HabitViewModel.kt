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
import com.example.data.model.TrackingType
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

/**
 * Unified UI State representation for Habit Tracker
 */
data class HabitUiState(
    val habits: List<Habit> = emptyList(),
    val filteredHabits: List<Habit> = emptyList(),
    val allLogs: List<HabitLog> = emptyList(),
    val selectedCategory: String = "All",
    val availableCategories: List<String> = listOf("All", "Fitness", "Health", "Mind", "Hobbies", "Archived"),
    val selectedHabitId: Long? = null,
    val selectedHabit: Habit? = null,
    val viewMode: DashboardViewMode = DashboardViewMode.CLASSIC,
    val selectedYear: Int = LocalDate.now().year,
    val themeMode: ThemeMode = ThemeMode.AMOLED,
    val currentScreen: AppScreen = AppScreen.DASHBOARD,
    val editingHabit: Habit? = null,
    val isLoading: Boolean = false,
    val userMessage: String? = null
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

    // Navigation & Screen State
    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _selectedHabitId = MutableStateFlow<Long?>(null)
    val selectedHabitId: StateFlow<Long?> = _selectedHabitId.asStateFlow()

    private val _viewMode = MutableStateFlow(DashboardViewMode.CLASSIC)
    val viewMode: StateFlow<DashboardViewMode> = _viewMode.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedYear = MutableStateFlow(LocalDate.now().year)
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _themeMode = MutableStateFlow(ThemeMode.AMOLED)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _editingHabit = MutableStateFlow<Habit?>(null)
    val editingHabit: StateFlow<Habit?> = _editingHabit.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // Habits stream from Room
    val allHabits: StateFlow<List<Habit>> = repository.allHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHabitsIncludingArchived: StateFlow<List<Habit>> = repository.allHabitsIncludingArchived
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLogs: StateFlow<List<HabitLog>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Habits based on Category and Archive status
    val filteredHabits: StateFlow<List<Habit>> = combine(
        allHabitsIncludingArchived,
        _selectedCategory
    ) { habits, category ->
        when (category) {
            "All" -> habits.filter { !it.isArchived }
            "Archived" -> habits.filter { it.isArchived }
            else -> habits.filter { !it.isArchived && it.category.equals(category, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Single Unified UI State via StateFlow
    val uiState: StateFlow<HabitUiState> = combine(
        allHabitsIncludingArchived,
        allLogs,
        _selectedCategory,
        _selectedHabitId,
        _viewMode
    ) { habits, logs, category, selectedId, vMode ->
        val filtered = when (category) {
            "All" -> habits.filter { !it.isArchived }
            "Archived" -> habits.filter { it.isArchived }
            else -> habits.filter { !it.isArchived && it.category.equals(category, ignoreCase = true) }
        }
        HabitUiState(
            habits = habits,
            filteredHabits = filtered,
            allLogs = logs,
            selectedCategory = category,
            selectedHabitId = selectedId,
            selectedHabit = habits.firstOrNull { it.id == selectedId },
            viewMode = vMode,
            selectedYear = _selectedYear.value,
            themeMode = _themeMode.value,
            currentScreen = _currentScreen.value,
            editingHabit = _editingHabit.value,
            isLoading = _isLoading.value,
            userMessage = _userMessage.value
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        HabitUiState()
    )

    // Navigation Actions
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

    fun clearUserMessage() {
        _userMessage.value = null
    }

    // CRUD: CREATE & UPDATE
    fun startCreateHabit() {
        _editingHabit.value = null
        navigateTo(AppScreen.EDIT_HABIT)
    }

    fun startEditHabit(habit: Habit) {
        _editingHabit.value = habit
        navigateTo(AppScreen.EDIT_HABIT, habit.id)
    }

    fun saveHabit(habit: Habit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val id = repository.saveHabit(habit)
                _selectedHabitId.value = id
                _userMessage.value = "Habit saved successfully"
                _currentScreen.value = AppScreen.DASHBOARD
            } catch (e: Exception) {
                _userMessage.value = "Failed to save habit: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createHabit(
        name: String,
        description: String = "",
        iconName: String = "check",
        colorHex: Long = 0xFF8AE034,
        habitType: HabitType = HabitType.BUILD,
        trackingType: TrackingType = TrackingType.YES_NO,
        targetValue: Float = 1.0f,
        unit: String = "",
        frequencyDaysPerWeek: Int = 7,
        category: String = "Health"
    ) {
        val nextOrder = (allHabits.value.maxOfOrNull { it.displayOrder } ?: 0) + 1
        val newHabit = Habit(
            name = name,
            description = description,
            iconName = iconName,
            colorHex = colorHex,
            habitType = habitType,
            trackingType = trackingType,
            targetValue = targetValue,
            unit = unit,
            frequencyDaysPerWeek = frequencyDaysPerWeek,
            category = category,
            displayOrder = nextOrder
        )
        saveHabit(newHabit)
    }

    // CRUD: DELETE
    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteHabit(habit)
                if (_selectedHabitId.value == habit.id) {
                    _selectedHabitId.value = null
                }
                _userMessage.value = "Habit deleted"
                _currentScreen.value = AppScreen.DASHBOARD
            } catch (e: Exception) {
                _userMessage.value = "Failed to delete habit: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ARCHIVE & UNARCHIVE
    fun toggleArchiveHabit(habit: Habit) {
        viewModelScope.launch {
            val newArchivedStatus = !habit.isArchived
            repository.setHabitArchived(habit.id, newArchivedStatus)
            _userMessage.value = if (newArchivedStatus) "Habit archived" else "Habit restored"
        }
    }

    // REORDERING
    fun moveHabit(fromIndex: Int, toIndex: Int) {
        val currentList = filteredHabits.value.toMutableList()
        if (fromIndex in currentList.indices && toIndex in currentList.indices) {
            val moved = currentList.removeAt(fromIndex)
            currentList.add(toIndex, moved)
            viewModelScope.launch {
                repository.reorderHabits(currentList)
            }
        }
    }

    // LOGS & PROGRESS CRUD
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

    // NOTES CRUD
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
            _userMessage.value = "Note saved"
        }
    }

    fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            repository.deleteNote(noteId)
            _userMessage.value = "Note deleted"
        }
    }

    // REMINDERS CRUD
    fun getRemindersForHabit(habitId: Long) = repository.getRemindersForHabit(habitId)

    fun saveReminder(reminder: HabitReminder) {
        viewModelScope.launch {
            repository.saveReminder(reminder)
            _userMessage.value = "Reminder updated"
        }
    }

    fun deleteReminder(reminderId: Long) {
        viewModelScope.launch {
            repository.deleteReminder(reminderId)
            _userMessage.value = "Reminder removed"
        }
    }

    // DATABASE MANAGEMENT
    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            _selectedHabitId.value = null
            _userMessage.value = "Database cleared"
            _currentScreen.value = AppScreen.DASHBOARD
        }
    }

    fun reloadSampleData() {
        viewModelScope.launch {
            repository.reloadSampleData()
            _userMessage.value = "Demo habits reloaded"
            _currentScreen.value = AppScreen.DASHBOARD
        }
    }

    // STREAK & STATS COMPUTATIONS
    fun calculateStats(habit: Habit, logs: List<HabitLog>, year: Int = LocalDate.now().year): HabitStats {
        val habitLogs = logs.filter { it.habitId == habit.id }
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val logMap = habitLogs.associateBy { it.date }

        val today = LocalDate.now()

        // Current Streak
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

        // Best Streak (past 365 days)
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
