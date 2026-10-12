# Implementation Plan: Interactive Notifications, Gamification & Home Screen Widget

Elevate the Habit Tracker with actionable Android system notifications, streak freeze protections, milestone achievement badges, and an interactive home screen widget.

---

### User Review & Critical Decisions

> [!IMPORTANT]
> The following directions were confirmed during the interactive planning session:
> - **Primary Focus**: Interactive system notifications and an interactive home screen widget for fast habit logging.
> - **Reminder Delivery**: System notifications powered by Android `NotificationManager` and `AlarmManager` with direct "Complete" quick-action buttons right from the notification drawer/lock screen.
> - **Motivation Mechanics**:
>   1. **Streak Freeze Protection**: Bankable freeze tokens that safeguard active streaks from breaking when a day is accidentally skipped or during rest days.
>   2. **Milestone Achievement Badges**: Unlockable achievement badges with celebration dialogues/cards for hitting 3, 7, 14, 30, and 100-day streaks.

---

## 1. Feature Specifications

### A. Real System Notifications with Quick-Complete Actions
- **AlarmManager & NotificationChannel**:
  - Register a dedicated Notification Channel (`habit_reminders_channel`) with high importance.
  - Set exact/inexact daily alarms using `AlarmManager` for scheduled habit reminder times.
  - Implement `HabitNotificationReceiver: BroadcastReceiver` that triggers when the alarm fires, building an expandable notification displaying habit title, category icon, current streak count, and motivation quote.
- **Quick-Complete Notification Action**:
  - Add an action button labeled **"✓ Complete"** to each notification.
  - Add a dedicated `HabitQuickActionReceiver: BroadcastReceiver` that logs completion into the Room SQLite database directly in background without opening the app, updates streak calculations, and dismisses/updates the notification with a positive confirmation toast/message.
- **Boot Persistence**:
  - Add `BootReceiver: BroadcastReceiver` listening to `BOOT_COMPLETED` so scheduled alarms are automatically reinstated when the device restarts.

### B. Streak Freeze Protection Mechanism
- **Data Model**:
  - Add `streakFreezesAvailable: Int` and `frozenDates: List<String>` tracking to user statistics / habit model.
  - Users earn freeze tokens (capped at 2 max) for sustained consistency (e.g., maintaining 7 active days awards 1 freeze token).
- **Streak Evaluation Logic**:
  - If a user misses completing a habit on a given date, the freeze token automatically intervenes (or user can manually activate freeze mode), preventing the streak from resetting to 0.
  - UI visual indicator: Icy blue frost badge on streak counters, indicating "Streak Frozen - Protected!" instead of showing broken streak.
  - Streak Freeze bank counter in the dashboard top app bar or streak card.

### C. Milestone Achievement Badges
- **Badge Types & Tiers**:
  - **First Step**: First habit ever completed.
  - **Consistency Trio**: 3-day streak across any habit.
  - **Weekly Warrior**: 7-day unbroken streak.
  - **Fortnight Focus**: 14-day unbroken streak.
  - **Monthly Habit Master**: 30-day streak milestone.
  - **Centurion**: 100-day milestone.
  - **Streak Savior**: Successfully applied a streak freeze to rescue a streak.
- **Badges Screen & Celebration**:
  - Visual badges grid in Analytics or dedicated "Achievements" tab with locked/unlocked visual states, unlock dates, and progress percentages.
  - Animated celebration dialog with confetti / celebratory banner upon unlocking a new badge during app usage.

### D. Home Screen Widget (Android AppWidget / Glance)
- **Compact & Interactive Habit Widget**:
  - Displays today's top pending habits with checkmark circles.
  - Tapping the habit checkmark immediately records completion and updates the widget state.
  - Tapping habit title launches `MainActivity` directly to that habit's detail page.
  - Displays today's overall progress bar (e.g., "3 of 5 completed - 60%").

---

## 2. Technical Architecture & File Plan

### 1. Data Layer Enhancements
- `app/src/main/java/com/example/data/model/Badge.kt`: Data class defining badges, icons, requirements, unlock status, and earned timestamps.
- `app/src/main/java/com/example/data/model/StreakFreeze.kt`: Model and logic for tracking freeze inventory and freeze usage history.
- `app/src/main/java/com/example/data/local/HabitDao.kt`: Add quick-complete helper queries and badge unlock persistence.

### 2. Notification & Receiver Infrastructure
- `app/src/main/java/com/example/receiver/HabitAlarmReceiver.kt`: Fires notifications at configured reminder times.
- `app/src/main/java/com/example/receiver/HabitActionReceiver.kt`: Handles "Complete" action button clicks directly from notification tray via coroutine scope and `HabitRepository`.
- `app/src/main/java/com/example/receiver/BootCompletedReceiver.kt`: Reschedules alarms on device boot.
- `app/src/main/java/com/example/util/NotificationHelper.kt`: Utility for creating channels, posting notifications, and formatting PendingIntents.

### 3. Widget Implementation
- `app/src/main/java/com/example/widget/HabitWidgetProvider.kt`: AppWidgetProvider managing widget views, remote clicks, and auto-updating upon database changes.
- `app/src/main/res/layout/widget_habit_tracker.xml` and `res/xml/habit_widget_info.xml`: AppWidget layout and provider configuration metadata.

### 4. UI & ViewModel Updates
- `app/src/main/java/com/example/ui/viewmodel/HabitViewModel.kt`:
  - Expose badges list, streak freeze balance, and badge unlock event channel.
  - Integrate freeze consumption inside streak calculation routine.
- `app/src/main/java/com/example/ui/screens/BadgesScreen.kt`: Dedicated achievements showcase with card grid, tier badges, and progress bars.
- `app/src/main/java/com/example/ui/components/StreakFreezeBanner.kt`: Visual freeze bank badge and rescue indicators on habit cards.
- `app/src/main/java/com/example/ui/components/MilestoneCelebrationDialog.kt`: Confetti celebration modal when a new badge is attained.

---

## 3. Verification & Testing Plan
- **Notification Verification**: Test trigger of system notification, verify notification permissions (`POST_NOTIFICATIONS`), and verify clicking "Complete" logs completion and updates Room database.
- **Streak Freeze Verification**: Unit/Robolectric test verifying that a missed day consumes a freeze token if available and preserves `currentStreak`.
- **Milestone Badges Verification**: Verify badge unlock conditions trigger accurately when streak thresholds (3, 7, 14, 30, 100) are met.
- **Widget Verification**: Test widget receiver updates when habits are checked or added.
- **Compilation Check**: Run `compile_applet` to verify clean compilation with all receivers and manifest declarations.
