# Habit Tracker 🎯

> **When your grid looks this good, you will not want to stop. Fill the tiles, protect your streak, and let consistency do the heavy lifting.**

Habit Tracker is a modern, privacy-first Android application designed with a focus on visual consistency. Inspired by contribution graphs, every habit is represented by a tile grid that lights up as you make progress. 

Whether you want to work out daily, read every night, meditate, drink more water, save money, or quit smoking, Habit Tracker provides a simple, satisfying loop: **do it, log it, watch the grid grow.**

---

## ✨ Features

### 1. Visual Contribution Tile Grids
- **Contribution Graph Aesthetics**: Every habit features a GitHub-style tile matrix spanning months of daily activity.
- **Micro-Interactions & Color Feedback**: Completed days light up in the habit's accent color; partial progress reflects through opacity/progress rings.
- **Pure Canvas Performance**: High-performance custom Canvas rendering renders months of data smoothly at 60/120 FPS.

### 2. Three Flexible Dashboard Views
Switch seamlessly between views via the bottom floating pill:
- **Classic View**: Individual habit cards featuring full contribution grids and quick-action completion buttons.
- **Compact View (Routine at a Glance)**: A horizontal 5-day table with weekday columns, allowing rapid multi-habit check-ins.
- **Split View**: Metric-focused cards highlighting current streaks, completion percentages, and daily actions.

### 3. Build Good Habits & Quit Bad Ones
- **Build A Habit**: Mark days complete and watch your positive streak multiply.
- **Quit A Habit**: Every day counts as clean automatically until a slip is logged. Track clean days in a row or stay under a daily maximum threshold.

### 4. Track Your Habits Your Way
- **Yes / No**: Quick one-tap checkmark.
- **Count**: Track reps, glasses, or pages with an interactive circular progress indicator.
- **Measure**: Measure continuous amounts with custom units (e.g., `2.0 L` of water, `7.5 km` running).

### 5. In-Depth Statistics & Analytics
- **Multi-Habit Switcher**: Quickly switch between habits to view analytics.
- **Yearly Navigator**: Explore historical activity by year (`< 2026 >`).
- **Key Metrics**: Total completions, overall completion rate, current streak, and all-time best streak.
- **Completions / Month Curve**: Smooth cubic Bézier curved area chart with gradient glow.
- **Milestone Snapshot Sharing**: Save and share milestone progress cards.

### 6. Interactive Monthly Calendar & Daily Notes
- **Full Monthly Calendar**: Navigate month-by-month (`< Month Year >`) with status dot indicators on completed days.
- **Daily Reflection Journal**: Long-press any calendar day or tap the notes icon to record reflections, wins, or obstacles.

### 7. Timely Scheduled Reminders
- Schedule up to **3 friendly reminders** per habit.
- Toggle days of the week individually (Mon–Sun) with custom 12h/24h time pickers.

### 8. Pure Black AMOLED & Color Customization
- **16 Vibrant Habit Colors**: Coral, amber, lime, cyan, periwinkle, lavender, rose, and more.
- **Theme Modes**: **Pure Black (AMOLED)** for OLED battery savings, **Dark Slate**, and **Light Mode**.
- **Icon Library**: Curated icons covering health, fitness, mindfulness, learning, and lifestyle.

### 9. 🔒 Private by Design & 100% Offline
- **Zero Accounts. Zero Tracking. Zero Ads.**
- All habits, logs, notes, and reminders are stored exclusively on your device using a local **SQLite Room Database**.
- Functions completely offline without requiring internet permissions.

---

## 🛠️ Architecture & Tech Stack

- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material Design 3 (M3).
- **Architecture Pattern**: MVVM (Model-View-ViewModel) with Clean Architecture principles.
- **Local Database**: [Android Room Database](https://developer.android.com/training/data-storage/room) powered by Kotlin Symbol Processing (KSP).
- **Asynchronous Flow**: Kotlin Coroutines & reactive `StateFlow` / `Flow`.
- **Canvas Rendering**: Custom Compose `DrawScope` implementation for contribution grids and Bézier curve graphs.
- **Language**: 100% Kotlin.
- **Minimum SDK**: Android 7.0 (API level 24).
- **Target SDK**: Android 16 (API level 36).

---

## 📁 Project Structure

```
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/
│   │   │   │   ├── MainActivity.kt               # Navigation host & theme wrapper
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/
│   │   │   │   │   │   ├── AppDatabase.kt        # Room database singleton & converters
│   │   │   │   │   │   └── HabitDaos.kt          # DAO interfaces (Habit, Log, Note, Reminder)
│   │   │   │   │   ├── model/
│   │   │   │   │   │   └── HabitModels.kt       # Room Entities & Domain models
│   │   │   │   │   └── repository/
│   │   │   │   │       └── HabitRepository.kt    # Data repository & seeding logic
│   │   │   │   └── ui/
│   │   │   │       ├── components/               # Custom UI Components
│   │   │   │       │   ├── ContributionGrid.kt   # Custom Canvas tile graph
│   │   │   │       │   ├── HabitCard.kt          # Classic view habit card
│   │   │   │       │   ├── CompactHabitRow.kt    # 5-day routine row
│   │   │   │       │   ├── MonthCalendarView.kt  # Monthly interactive calendar
│   │   │   │       │   ├── MonthlyCompletionChart.kt # Smooth Bezier curve chart
│   │   │   │       │   ├── HabitActionButton.kt  # Checkmark & progress circle
│   │   │   │       │   └── HabitIcons.kt         # Icon catalog
│   │   │   │       ├── screens/                  # Application screens
│   │   │   │       │   ├── DashboardScreen.kt    # Main dashboard (Classic, Compact, Split)
│   │   │   │       │   ├── HabitDetailScreen.kt  # Grid, streak, calendar, and notes
│   │   │   │       │   ├── AnalyticsScreen.kt    # Stats, metrics, and monthly chart
│   │   │   │       │   ├── NotesScreen.kt        # Daily timeline notes list
│   │   │   │       │   ├── RemindersScreen.kt    # Scheduled notifications configuration
│   │   │   │       │   ├── EditHabitScreen.kt    # Create & edit habit modal
│   │   │   │       │   └── SettingsScreen.kt     # Themes, widget preview & DB controls
│   │   │   │       ├── theme/                    # Theme, Color palette & Typography
│   │   │   │       └── viewmodel/
│   │   │   │           └── HabitViewModel.kt     # State management & streak calculations
│   │   │   └── res/                              # Drawables, mipmaps, strings, and values
│   │   └── test/                                 # JVM Unit & Robolectric tests
├── .github/
│   └── workflows/
│       ├── build-apk.yml                         # Automated APK release & GitHub Release workflow
│       └── ci-checks.yml                         # PR test validation workflow
├── build.gradle.kts                              # Root project configuration
├── settings.gradle.kts                           # Gradle settings
└── README.md                                     # Project documentation
```

---

## 🚀 Building & Running

### Prerequisites
- Android Studio Ladybug / Meerkat or IntelliJ IDEA with Android plugin.
- JDK 17 or higher.
- Android SDK with API 36 platforms installed.

### Clone and Build

1. **Clone the repository:**
   ```bash
   git clone <repo-url>
   cd habit-tracker
   ```

2. **Run Unit & Robolectric Tests:**
   ```bash
   ./gradlew testDebugUnitTest
   ```

3. **Assemble Debug APK:**
   ```bash
   ./gradlew assembleDebug
   ```
   The APK will be located at: `app/build/outputs/apk/debug/app-debug.apk`.

4. **Assemble Release APK:**
   ```bash
   ./gradlew assembleRelease
   ```
   The APK will be located at: `app/build/outputs/apk/release/app-release.apk`.

---

## ⚙️ CI/CD & Automated GitHub Releases

Automated GitHub Actions workflows are located in `.github/workflows/`:

- **Build Release APK (`build-apk.yml`)**:
  - Triggers on pushes to `main` / `master` and tags matching `v*`.
  - Compiles the optimized production **Release APK** (`HabitTracker-release.apk`).
  - **Consistent Signing Key for App Updates**: Utilizes a permanent 10,000-day RSA signing key (`release.keystore.base64`, or your custom `KEYSTORE_BASE64` secret), ensuring that every single build and release shares the **exact same cryptographic certificate**. This allows users to seamlessly update their installed app without signature conflict errors.
  - When a release tag is pushed (e.g., `git tag v1.0.0 && git push --tags`), it creates a **GitHub Release** with `HabitTracker-release.apk` attached.
  - Can be manually dispatched from the **Actions** tab in GitHub with a custom tag.
- **PR Checks (`ci-checks.yml`)**:
  - Validates all pull requests with JVM unit and Robolectric tests.

---

## 💾 Local Data Management

In the **Settings** screen, you can manage your local Room database:
- **Clear All Data (Start Blank)**: Removes all SQLite data so you can build your habit journey from scratch.
- **Reload Demo Habits**: Restores sample habits matching the design preview with pre-computed contribution grids and notes.
- **Export Habit Data**: Back up your habits and logs to JSON.

---

## 📄 License
This project is open-source and available under the [MIT License](LICENSE).
