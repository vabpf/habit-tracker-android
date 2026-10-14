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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.HabitReminder
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.LocalAppChrome
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.HabitViewModel
import java.util.Locale

@Composable
fun RemindersScreen(
    habitId: Long,
    viewModel: HabitViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(AppScreen.HABIT_DETAIL, habitId)
    }

    val context = LocalContext.current
    val habits by viewModel.allHabits.collectAsState()
    val habit = habits.firstOrNull { it.id == habitId }

    val remindersFlow = remember(habitId) { viewModel.getRemindersForHabit(habitId) }
    val reminders by remindersFlow.collectAsState(initial = emptyList())

    // Dialog state for adding or changing time
    var showTimeDialog by remember { mutableStateOf(false) }
    var selectedHour by remember { mutableIntStateOf(8) }
    var selectedMinute by remember { mutableIntStateOf(0) }
    var currentReminderToEdit by remember { mutableStateOf<HabitReminder?>(null) }

    val daysOfWeekLabels = listOf(
        1 to "Mon",
        2 to "Tue",
        3 to "Wed",
        4 to "Thu",
        5 to "Fri",
        6 to "Sat",
        7 to "Sun"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(AppScreen.HABIT_DETAIL, habitId) },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(LocalAppChrome.current.container)
                        .testTag("reminders_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Reminder (${reminders.size} / 3)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(modifier = Modifier.weight(1f))
                Box(modifier = Modifier.size(40.dp)) // balance layout
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Reminders repeat at these times, even if you have already logged today.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Reminders List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(reminders) { index, reminder ->
                    val activeDays = remember(reminder.daysOfWeek) {
                        reminder.daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Text(
                                text = "Reminder #${index + 1}",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Day of Week Toggle Pills
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                daysOfWeekLabels.forEach { (dayNum, label) ->
                                    val isSelected = activeDays.contains(dayNum)
                                    Box(
                                        modifier = Modifier
                                            .size(width = 40.dp, height = 36.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) BrandPrimary else LocalAppChrome.current.container
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) BrandPrimary else LocalAppChrome.current.border,
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                val newSet = if (isSelected) activeDays - dayNum else activeDays + dayNum
                                                val updatedStr = newSet.sorted().joinToString(",")
                                                viewModel.saveReminder(reminder.copy(daysOfWeek = updatedStr))
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Time Picker Row & Delete Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val isAm = reminder.hour < 12
                                val displayHour = when {
                                    reminder.hour == 0 -> 12
                                    reminder.hour > 12 -> reminder.hour - 12
                                    else -> reminder.hour
                                }
                                val amPm = if (isAm) "AM" else "PM"
                                val formattedTime = String.format(Locale.US, "%d:%02d %s", displayHour, reminder.minute, amPm)

                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(LocalAppChrome.current.containerAlt)
                                        .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                        .clickable {
                                            currentReminderToEdit = reminder
                                            selectedHour = reminder.hour
                                            selectedMinute = reminder.minute
                                            showTimeDialog = true
                                        }
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AccessTime,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = formattedTime,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                IconButton(
                                    onClick = { viewModel.deleteReminder(reminder.id) },
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(LocalAppChrome.current.containerAlt)
                                        .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Delete Reminder",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // + Add Reminder Button (if < 3)
            if (reminders.size < 3) {
                Button(
                    onClick = {
                        currentReminderToEdit = null
                        selectedHour = 8
                        selectedMinute = 0
                        showTimeDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(bottom = 16.dp)
                        .height(52.dp)
                        .testTag("add_reminder_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add Reminder",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    )
                }
            } else {
                Text(
                    text = "Maximum of 3 reminders reached for this habit.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }

    // Time Selection Dialog
    if (showTimeDialog) {
        val hours = (0..23).toList()
        val minutes = listOf(0, 15, 30, 45)

        AlertDialog(
            onDismissRequest = { showTimeDialog = false },
            title = {
                Text(
                    text = "Select Time",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Hour (24h format)",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(7, 8, 9, 12, 18, 20, 21, 22).forEach { h ->
                            val isSelected = selectedHour == h
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(CircleShape)
                                    .background(if (isSelected) BrandPrimary else LocalAppChrome.current.container)
                                    .clickable { selectedHour = h }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$h:00",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Minute",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        minutes.forEach { m ->
                            val isSelected = selectedMinute == m
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(CircleShape)
                                    .background(if (isSelected) BrandPrimary else LocalAppChrome.current.container)
                                    .clickable { selectedMinute = m }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = String.format(Locale.US, ":%02d", m),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val toSave = currentReminderToEdit?.copy(
                            hour = selectedHour,
                            minute = selectedMinute
                        ) ?: HabitReminder(
                            habitId = habitId,
                            hour = selectedHour,
                            minute = selectedMinute,
                            daysOfWeek = "1,2,3,4,5,6,7"
                        )
                        viewModel.saveReminder(toSave)
                        showTimeDialog = false
                        Toast.makeText(context, "Reminder scheduled", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                ) {
                    Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimeDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}
