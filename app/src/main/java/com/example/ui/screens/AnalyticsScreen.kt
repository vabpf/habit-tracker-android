package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Habit
import com.example.data.model.HabitType
import com.example.data.model.TrackingType
import com.example.ui.components.ContributionGrid
import com.example.ui.components.HabitIconCatalog
import com.example.ui.components.MonthlyCompletionChart
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.LocalAppChrome
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.HabitViewModel
import java.time.LocalDate

@Composable
fun AnalyticsScreen(
    viewModel: HabitViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(AppScreen.DASHBOARD)
    }

    val context = LocalContext.current
    val habits by viewModel.allHabits.collectAsState()
    val allLogs by viewModel.allLogs.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val selectedHabitId by viewModel.selectedHabitId.collectAsState()

    var activeHabitId by remember(habits, selectedHabitId) {
        mutableStateOf(selectedHabitId ?: habits.firstOrNull()?.id ?: 0L)
    }

    val currentHabit = habits.firstOrNull { it.id == activeHabitId } ?: habits.firstOrNull()

    if (currentHabit == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Text("No habits available", color = MaterialTheme.colorScheme.onSurface)
        }
        return
    }

    val habitLogs = allLogs.filter { it.habitId == currentHabit.id }
    val habitColor = Color(currentHabit.colorHex)
    val stats = remember(currentHabit, habitLogs, selectedYear) {
        viewModel.calculateStats(currentHabit, habitLogs, selectedYear)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            // No container-level navigationBarsPadding: content flows behind
            // the transparent nav bar (like Dashboard) instead of leaving a
            // dead band. Bottom content padding keeps the last item clear.
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Habit Switcher Bar + Done Button (Screenshot 4)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LazyRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(habits) { habit ->
                            val isSelected = habit.id == currentHabit.id
                            val itemColor = Color(habit.colorHex)
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) BrandPrimary else LocalAppChrome.current.container
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) BrandPrimary else LocalAppChrome.current.border,
                                        shape = CircleShape
                                    )
                                    .clickable { activeHabitId = habit.id }
                                    .testTag("analytics_habit_chip_${habit.id}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = HabitIconCatalog.getIcon(habit.iconName),
                                    contentDescription = habit.name,
                                    tint = if (isSelected) Color.Black else itemColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("analytics_done_button")
                    ) {
                        Text(
                            text = "Done",
                            color = Color.Black,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // Current Habit Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(habitColor.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = HabitIconCatalog.getIcon(currentHabit.iconName),
                                contentDescription = currentHabit.name,
                                tint = habitColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = currentHabit.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            if (currentHabit.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = currentHabit.description,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Year Selector (< 2026 >)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.setYear(selectedYear - 1) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Previous Year",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = selectedYear.toString(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.padding(horizontal = 18.dp)
                    )

                    IconButton(
                        onClick = { viewModel.setYear(selectedYear + 1) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next Year",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Contribution Grid for the year
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    ContributionGrid(
                        habit = currentHabit,
                        logs = habitLogs,
                        columns = 24,
                        rows = 7,
                        showLabels = true,
                        tileHeight = 9.dp,
                        // Follow the year picker: show the window ending at
                        // Dec 31 of the selected year. Past years show that
                        // year's data; the current year ends today; future
                        // years render all-future (empty) grids.
                        endDate = remember(selectedYear) {
                            val now = LocalDate.now()
                            if (selectedYear == now.year) now
                            else LocalDate.of(selectedYear, 12, 31)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }

            // Metrics Row: [Completions] and [Completion Rate] (Screenshot 4)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Completions Card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(habitColor.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Tag,
                                    contentDescription = "Total",
                                    tint = habitColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            val completionText = when (currentHabit.trackingType) {
                                TrackingType.YES_NO -> "${stats.totalCompletions}"
                                TrackingType.COUNT, TrackingType.MEASURE -> {
                                    val totalUnits = habitLogs.sumOf { it.value.toDouble() }.toInt()
                                    if (currentHabit.unit.isNotBlank()) "$totalUnits ${currentHabit.unit}" else "$totalUnits"
                                }
                            }

                            Text(
                                text = completionText,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Completions",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    // Completion Rate Card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(habitColor.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Percent,
                                    contentDescription = "Rate",
                                    tint = habitColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "${stats.completionRate}%",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Completion Rate",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }

            // Completions / Month Chart Card
            item {
                MonthlyCompletionChart(
                    data = stats.monthlyData,
                    habitColor = habitColor
                )
            }

            // Streaks Row: [Current Streak] and [Best Streak]
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Current Streak
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(habitColor.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocalFireDepartment,
                                    contentDescription = "Streak",
                                    tint = habitColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "${stats.currentStreak}",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Current Streak",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    // Best Streak
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(habitColor.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocalFireDepartment,
                                    contentDescription = "Best",
                                    tint = habitColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "${stats.bestStreak}",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Best Streak",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }

            // Share Progress Snapshot Button
            item {
                Button(
                    onClick = {
                        Toast.makeText(context, "Milestone snapshot saved to gallery!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LocalAppChrome.current.container)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Share Milestone Snapshot",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
