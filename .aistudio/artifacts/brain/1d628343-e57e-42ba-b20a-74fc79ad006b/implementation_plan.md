# Habit ViewModel & DAO Integration Plan

Establish a robust, reactive ViewModel and Repository architecture for Habit Tracker that interacts with the Room SQLite database, encapsulates business logic, and exposes a single unified UI state to Jetpack Compose.

---

### User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural decisions were confirmed during the interactive planning session:
> - **Data Access Architecture**: Repository pattern wrapping the Room `HabitDao` rather than direct DAO exposure to the ViewModel.
> - **UI State Modeling**: Single immutable `HabitUiState` data class exposed via a `StateFlow` stream (`WhileSubscribed(5000)`).
> - **Extended Operations**: In addition to standard CRUD (Create, Read, Update, Delete), the ViewModel will support habit reordering, category filtering, and archive/restore functionality.

---

## 1. Overview & Core Concept

- **What It Does**: Centralizes habit state management and Room database operations in a dedicated `HabitViewModel` and `HabitRepository`. The UI observes an immutable state representation that updates in real time whenever SQLite tables change.
- **Target Experience**: Seamless, glitch-free UI updates with instant response times, offline-first reliability, and clean separation between Android framework lifecycles and business logic.
- **Key Value**: Provides predictable state flow, eliminating out-of-sync states between filtered views, detail screens, and database writes.

---

## 2. User Experience & Visual Design

- **Unified State Collection**: The UI collects `uiState` with lifecycle safety (`collectAsStateWithLifecycle()`), ensuring operations pause when the app is in the background.
- **Instant Optimistic Feedback**: When checking off habits, reordering items, or changing categories, updates appear immediately in Compose while saving asynchronously in SQLite.
- **Category & Archive Filtering**:
  - Filter chips ("All", "Fitness", "Health", "Mind", "Hobbies", "Archived") update the visible list without reloading or querying the database from scratch.
  - Archived habits can be toggled in and out of the active dashboard to keep routine views clean.
- **Error Handling & Loading States**:
  - Subtle feedback toasts / snackbar messages for errors (e.g., validation failures on empty habit names).

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Repository Layer Abstraction**
  - *Chosen Approach*: `HabitRepository` acts as the single source of truth between `HabitDao` and `HabitViewModel`.
  - *Why*: Encapsulates query logic, transactions, and seeding routines, preventing the ViewModel from depending directly on Room internal mechanisms.
  - *Alternatives Considered*: Direct DAO access in ViewModel (rejected: tight coupling, harder to mock or test).

- **Decision 2: Single Unified UI State Data Class vs. Multiple StateFlows**
  - *Chosen Approach*: `data class HabitUiState(...)` exposed through `val uiState: StateFlow<HabitUiState>`.
  - *Why*: Guarantees atomic, cohesive state changes. Eliminates race conditions where filtered habits and selected categories update across separate frames.
  - *Alternatives Considered*: Multiple independent `MutableStateFlow` streams (already partially used, now unified into one cohesive model).

- **Decision 3: Archive Flag Support**
  - *Chosen Approach*: Add `isArchived` toggle methods (`archiveHabit(id)`, `unarchiveHabit(id)`) and filter active habits by default.
  - *Why*: Users often want to pause or retire habits without losing their historic contribution tile grids, streaks, and journal notes.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────┐
│                   Jetpack Compose UI                   │
│   (DashboardScreen, DetailScreen, EditHabitScreen)     │
└───────────────────────────▲────────────────────────────┘
                            │ collectAsStateWithLifecycle()
                            │ emits HabitUiState
┌───────────────────────────┴────────────────────────────┐
│                    HabitViewModel                      │
│  - uiState: StateFlow<HabitUiState>                    │
│  - createHabit(habit), updateHabit(habit)              │
│  - deleteHabit(habit), archiveHabit(id)                │
│  - reorderHabits(fromIndex, toIndex)                   │
│  - setCategory(category), filterActive()               │
└───────────────────────────▲────────────────────────────┘
                            │ Coroutine Dispatchers / Flow
┌───────────────────────────┴────────────────────────────┐
│                   HabitRepository                      │
│  - allHabits: Flow<List<Habit>>                        │
│  - insert/update/delete/reorder/archive operations     │
└───────────────────────────▲────────────────────────────┘
                            │ Room DAO Queries
┌───────────────────────────┴────────────────────────────┐
│              HabitDao (SQLite Database)                │
│  @Query, @Insert, @Update, @Delete                     │
└────────────────────────────────────────────────────────┘
```

### Unified UI State Definition

```kotlin
data class HabitUiState(
    val habits: List<Habit> = emptyList(),
    val filteredHabits: List<Habit> = emptyList(),
    val selectedCategory: String = "All",
    val availableCategories: List<String> = listOf("All", "Fitness", "Health", "Mind", "Hobbies"),
    val showArchived: Boolean = false,
    val selectedHabit: Habit? = null,
    val isLoading: Boolean = false,
    val userMessage: String? = null
)
```

### Interactive Methods on `HabitViewModel`

1. **Create & Update**:
   - `fun saveHabit(name: String, description: String, icon: String, color: Long, type: HabitType, tracking: TrackingType, target: Float, unit: String, frequency: Int, category: String, habitId: Long? = null)`
2. **Read & Filter**:
   - Combined reactive stream merging `repository.allHabits`, `selectedCategory`, and `showArchived` into `uiState`.
3. **Delete & Archive**:
   - `fun deleteHabit(habit: Habit)`
   - `fun toggleArchiveHabit(habit: Habit)`
4. **Reorder**:
   - `fun moveHabit(fromIndex: Int, toIndex: Int)` updating `displayOrder` in SQLite.
