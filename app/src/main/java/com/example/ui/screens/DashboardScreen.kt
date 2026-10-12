package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Habit
import com.example.data.model.HabitLog
import com.example.ui.components.MilestoneCelebrationDialog
import com.example.ui.components.StreakFreezeBanner
import com.example.ui.components.StreakFreezeVaultDialog
import com.example.ui.components.CompactHabitRow
import com.example.ui.components.HabitActionButton
import com.example.ui.components.HabitCard
import com.example.ui.components.HabitIconCatalog
import com.example.ui.theme.BrandPrimary
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.DashboardViewMode
import com.example.ui.viewmodel.HabitViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: HabitViewModel,
    modifier: Modifier = Modifier
) {
    val habits by viewModel.filteredHabits.collectAsState()
    val allLogs by viewModel.allLogs.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val streakFreezeState by viewModel.streakFreezeState.collectAsState()
    val newlyUnlockedBadge by viewModel.newlyUnlockedBadge.collectAsState()

    var showVaultDialog by remember { mutableStateOf(false) }

    val today = remember { LocalDate.now() }
    val formatter = remember { DateTimeFormatter.ISO_LOCAL_DATE }
    val todayStr = remember(today) { today.format(formatter) }

    // Map habitId to map of date -> HabitLog
    val logsByHabit = remember(allLogs) {
        allLogs.groupBy { it.habitId }
    }

    // Last 5 days for Compact View (Screenshot 2)
    val last5Days = remember(today) {
        (4 downTo 0).map { today.minusDays(it.toLong()) }
    }

    val uiState by viewModel.uiState.collectAsState()
    val categories = uiState.availableCategories

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Settings Icon (Left)
                IconButton(
                    onClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E212A))
                        .testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Right actions: Achievements + Analytics + Add Habit
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Achievements Trophy Button
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.BADGES) },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E212A))
                            .testTag("achievements_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.EmojiEvents,
                            contentDescription = "Achievements",
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Analytics Button
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.ANALYTICS) },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E212A))
                            .testTag("analytics_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Analytics,
                            contentDescription = "Statistics",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Add Habit Button (Purple circle with plus)
                    IconButton(
                        onClick = { viewModel.startCreateHabit() },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(BrandPrimary)
                            .testTag("add_habit_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add Habit",
                            tint = Color.Black,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Streak Freeze Protection Banner
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                StreakFreezeBanner(
                    freezeState = streakFreezeState,
                    onOpenVault = { showVaultDialog = true }
                )
            }

            // Categories Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setCategory(category) },
                        label = {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color(0xFF161820),
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedContainerColor = BrandPrimary,
                            selectedLabelColor = Color.Black
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = Color(0xFF282C38),
                            selectedBorderColor = BrandPrimary
                        )
                    )
                }
            }

            // Main Content Area based on View Mode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                    if (habits.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(BrandPrimary.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = null,
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No habits yet",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Build your routine. Fill the tiles, protect your streak, and watch the grid grow.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { viewModel.startCreateHabit() },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                            ) {
                                Text(
                                    text = "Create First Habit",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    } else when (viewMode) {
                        DashboardViewMode.CLASSIC -> {
                            // Classic View: Cards with full contribution grids
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 8.dp,
                                bottom = 88.dp // space for floating bottom switcher
                            ),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(habits, key = { it.id }) { habit ->
                                val habitLogs = logsByHabit[habit.id] ?: emptyList()
                                val todayLog = habitLogs.firstOrNull { it.date == todayStr }

                                HabitCard(
                                    habit = habit,
                                    logs = habitLogs,
                                    todayLog = todayLog,
                                    onCardClick = {
                                        viewModel.navigateTo(AppScreen.HABIT_DETAIL, habit.id)
                                    },
                                    onActionClick = {
                                        viewModel.toggleHabitDay(habit, today)
                                    }
                                )
                            }
                        }
                    }

                    DashboardViewMode.COMPACT -> {
                        // Compact View: Routine at a glance with 5 days table
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                        ) {
                            // Top Date Summary Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF1E212A))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Last 5 days",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }

                                val startMonth = last5Days.first().month.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase()
                                val endMonth = last5Days.last().month.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase()
                                val rangeStr = if (startMonth == endMonth) {
                                    "$startMonth ${last5Days.first().dayOfMonth} — ${last5Days.last().dayOfMonth}"
                                } else {
                                    "$startMonth ${last5Days.first().dayOfMonth} — $endMonth ${last5Days.last().dayOfMonth}"
                                }

                                Text(
                                    text = rangeStr,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        letterSpacing = 1.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }

                            // Table Columns Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${habits.size} HABITS",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        letterSpacing = 1.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.weight(1f)
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    last5Days.forEach { date ->
                                        val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(2)
                                        val isToday = date == today
                                        Column(
                                            modifier = Modifier.width(30.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = dayName,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = if (isToday) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 10.sp,
                                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                                )
                                            )
                                            Text(
                                                text = date.dayOfMonth.toString(),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = if (isToday) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            // Rows List
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 88.dp)
                            ) {
                                items(habits, key = { it.id }) { habit ->
                                    val habitLogs = logsByHabit[habit.id] ?: emptyList()
                                    val logsMap = habitLogs.associateBy { it.date }

                                    CompactHabitRow(
                                        habit = habit,
                                        days = last5Days,
                                        logsMap = logsMap,
                                        onDayClick = { clickedDate ->
                                            viewModel.toggleHabitDay(habit, clickedDate)
                                        },
                                        onRowClick = {
                                            viewModel.navigateTo(AppScreen.HABIT_DETAIL, habit.id)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    DashboardViewMode.SPLIT -> {
                        // Split / Metric View: Compact grid cards showing streak + today's status
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 8.dp,
                                bottom = 88.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(habits, key = { it.id }) { habit ->
                                val habitLogs = logsByHabit[habit.id] ?: emptyList()
                                val todayLog = habitLogs.firstOrNull { it.date == todayStr }
                                val stats = viewModel.calculateStats(habit, habitLogs)
                                val habitColor = Color(habit.colorHex)

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.navigateTo(AppScreen.HABIT_DETAIL, habit.id)
                                        },
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    border = CardDefaults.outlinedCardBorder()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(habitColor.copy(alpha = 0.16f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = HabitIconCatalog.getIcon(habit.iconName),
                                                contentDescription = habit.name,
                                                tint = habitColor,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = habit.name,
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Filled.LocalFireDepartment,
                                                    contentDescription = "Streak",
                                                    tint = habitColor,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(
                                                    text = "${stats.currentStreak} day streak",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        color = habitColor,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                )
                                                Text(
                                                    text = " • ${stats.completionRate}% rate",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                )
                                            }
                                        }

                                        HabitActionButton(
                                            habit = habit,
                                            currentValue = todayLog?.value ?: 0f,
                                            isCompleted = todayLog?.isCompleted == true,
                                            onActionClick = {
                                                viewModel.toggleHabitDay(habit, today)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Pill View Mode Switcher at bottom center (Screenshot 1 & 2)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(Color(0xE622252E))
                .border(1.dp, Color(0xFF333742), RoundedCornerShape(26.dp))
                .padding(horizontal = 6.dp, vertical = 6.dp)
                .testTag("view_mode_switcher")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Classic Grid View
                val isClassic = viewMode == DashboardViewMode.CLASSIC
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isClassic) Color(0xFF3A3F4E) else Color.Transparent)
                        .clickable { viewModel.setViewMode(DashboardViewMode.CLASSIC) }
                        .testTag("view_mode_classic"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.GridView,
                        contentDescription = "Classic View",
                        tint = if (isClassic) Color.White else Color(0xFF8E93A3),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Compact List View
                val isCompact = viewMode == DashboardViewMode.COMPACT
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isCompact) Color(0xFF3A3F4E) else Color.Transparent)
                        .clickable { viewModel.setViewMode(DashboardViewMode.COMPACT) }
                        .testTag("view_mode_compact"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = "Compact View",
                        tint = if (isCompact) Color.White else Color(0xFF8E93A3),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Split View
                val isSplit = viewMode == DashboardViewMode.SPLIT
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSplit) Color(0xFF3A3F4E) else Color.Transparent)
                        .clickable { viewModel.setViewMode(DashboardViewMode.SPLIT) }
                        .testTag("view_mode_split"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.ViewAgenda,
                        contentDescription = "Split View",
                        tint = if (isSplit) Color.White else Color(0xFF8E93A3),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Vault Dialog
        if (showVaultDialog) {
            StreakFreezeVaultDialog(
                freezeState = streakFreezeState,
                onToggleAutoFreeze = { viewModel.toggleAutoFreeze(it) },
                onUseFreezeToday = {
                    viewModel.useStreakFreezeToday()
                    showVaultDialog = false
                },
                onDismiss = { showVaultDialog = false }
            )
        }

        // Milestone Celebration Modal
        if (newlyUnlockedBadge != null) {
            MilestoneCelebrationDialog(
                badge = newlyUnlockedBadge,
                onDismiss = { viewModel.dismissMilestoneCelebration() }
            )
        }
    }
}
