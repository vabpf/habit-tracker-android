# Architecture Documentation — Habit Tracker

This document details the architectural decisions, database schema, state management, and rendering pipelines in Habit Tracker.

---

## 1. Clean MVVM Architecture

```
┌────────────────────────────────────────────────────────┐
│                      UI Layer                          │
│  Jetpack Compose Screens & Canvas Custom Components    │
└───────────────────────────▲────────────────────────────┘
                            │ StateFlow / Events
┌───────────────────────────┴────────────────────────────┐
│                   ViewModel Layer                      │
│   HabitViewModel (Streak Algorithm, Analytics & State) │
└───────────────────────────▲────────────────────────────┘
                            │ Coroutines / Flow
┌───────────────────────────┴────────────────────────────┐
│                  Repository Layer                      │
│    HabitRepository (Abstraction & Seed Dispatcher)     │
└───────────────────────────▲────────────────────────────┘
                            │ Room Queries
┌───────────────────────────┴────────────────────────────┐
│                    Data / Room Layer                   │
│   AppDatabase, DAOs (Habit, Log, Note, Reminder)       │
│                  SQLite On-Device DB                   │
└────────────────────────────────────────────────────────┘
```

---

## 2. Room Database Schema

### Table: `habits`
Represents an individual habit definition.
- `id` (INTEGER, Primary Key, Auto-Increment)
- `name` (TEXT): Title of the habit.
- `description` (TEXT): Subtitle or motive.
- `iconName` (TEXT): Key mapped in `HabitIconCatalog`.
- `colorHex` (INTEGER): ARGB color for tiles and accents.
- `habitType` (TEXT): `BUILD` or `QUIT`.
- `trackingType` (TEXT): `YES_NO`, `COUNT`, or `MEASURE`.
- `targetValue` (REAL): Target amount per day (e.g. `1.0`, `20.0`, `2.0`).
- `unit` (TEXT): Unit label (e.g. `"pages"`, `"L"`, `"km"`).
- `frequencyDaysPerWeek` (INTEGER): Scheduled days per week (`1` to `7`).
- `category` (TEXT): Filter category (`"Fitness"`, `"Health"`, `"Mind"`, etc.).
- `displayOrder` (INTEGER): Order index for sorting.
- `createdAt` (INTEGER): Timestamp in epoch milliseconds.
- `isArchived` (INTEGER): Archive flag (`0` or `1`).

### Table: `habit_logs`
Represents daily progress or slips.
- `id` (INTEGER, Primary Key, Auto-Increment)
- `habitId` (INTEGER, Foreign Index)
- `date` (TEXT, ISO-8601: `YYYY-MM-DD`): Unique composite index with `habitId`.
- `value` (REAL): Logged amount for the day.
- `isCompleted` (INTEGER): Boolean flag indicating completion.
- `timestamp` (INTEGER): Epoch milliseconds of the entry.

### Table: `habit_notes`
Daily reflections and journal entries.
- `id` (INTEGER, Primary Key, Auto-Increment)
- `habitId` (INTEGER, Index)
- `date` (TEXT, ISO-8601: `YYYY-MM-DD`)
- `content` (TEXT): Note content.
- `timestamp` (INTEGER): Epoch milliseconds.

### Table: `habit_reminders`
Scheduled daily notification times.
- `id` (INTEGER, Primary Key, Auto-Increment)
- `habitId` (INTEGER, Index)
- `hour` (INTEGER): `0`–`23`.
- `minute` (INTEGER): `0`–`59`.
- `daysOfWeek` (TEXT): Comma-separated day integers (e.g. `"1,2,3,4,5"` where `1` = Mon).
- `isEnabled` (INTEGER): Toggle flag.

---

## 3. Streak Calculation Algorithms

### Positive ("Build") Habits
1. **Current Streak**:
   - Checks if today is logged and completed. If yes, increments streak by 1 and steps backwards to yesterday.
   - If today is not yet logged, starts checking from yesterday (giving the user until the end of today to complete their habit without prematurely breaking their streak).
   - Iterates consecutively backwards day by day. Stops at the first incomplete day.
2. **Best Streak**:
   - Evaluates the past 365 days in chronological order.
   - Increments a running counter for each completed day and resets on an incomplete day.
   - The all-time high is tracked and compared against the current streak.

### Negative ("Quit") Habits
- **Clean Days Counter**:
  - Every day is considered clean by default unless a slip (`value > 0`) is logged.
  - The current streak counts consecutive clean days backwards until the most recent slip date.

---

## 4. Canvas Contribution Grid Pipeline

The tile grid in `ContributionGrid.kt` is drawn directly onto a hardware-accelerated Compose `Canvas` via `DrawScope`:
- **Coordinate Matrix**:
  - `columns` (typically 20–24 columns) × `rows` (7 days of the week).
  - Aligned such that the far right column corresponds to the current week.
- **Dynamic Geometry**:
  ```kotlin
  val cellWidth = (totalWidth - (columns - 1) * spacing) / columns
  val cellHeight = (totalHeight - (rows - 1) * spacing) / rows
  val cornerRadius = CornerRadius(cellWidth * 0.28f, cellHeight * 0.28f)
  ```
- **Rendering Passes**:
  1. Draw background/inactive rounded rectangle (`#1A1C24` / `TileInactiveDark`).
  2. If the day is completed, draw an overlapping rounded rectangle with the habit's accent color (with alpha adjusted for partial progress).
- **Zero Layout Overhead**: Avoids instantiating hundreds of Composable nodes or layouts, maintaining 60/120 FPS performance even when scrolling multiple cards.

---

## 5. Bézier Curve Analytics Chart

In `MonthlyCompletionChart.kt`, monthly totals across the selected year are rendered using a smooth cubic Bézier path:
- Points are computed by normalizing completion counts against the annual monthly maximum:
  $$Y = \text{Height} - (\frac{\text{Count}}{\text{Max}} \times (\text{Height} - \text{Padding}))$$
- Control points for each segment between $(x_0, y_0)$ and $(x_1, y_1)$ are placed at:
  $$\text{CP}_1 = (x_0 + \frac{x_1 - x_0}{2}, y_0), \quad \text{CP}_2 = (x_0 + \frac{x_1 - x_0}{2}, y_1)$$
- The path is filled using a vertical linear gradient brush transitioning from the habit's color to transparent black.
