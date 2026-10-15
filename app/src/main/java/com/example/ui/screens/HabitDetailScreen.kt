@file:OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)

package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Habit
import com.example.data.model.HabitType
import com.example.data.model.TrackingType
import com.example.ui.components.ContributionGrid
import com.example.ui.components.CircleIconButton
import com.example.ui.components.HabitIconCatalog
import com.example.ui.components.habitIconTransition
import com.example.ui.components.rememberShakeController
import com.example.ui.components.shake
import kotlinx.coroutines.launch
import com.example.ui.components.MonthCalendarView
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.HabitCoral
import com.example.ui.theme.LocalAppChrome
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.HabitViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun HabitDetailScreen(
    habitId: Long,
    viewModel: HabitViewModel,
    modifier: Modifier = Modifier,
    sharedScope: SharedTransitionScope? = null,
    animatedScope: AnimatedVisibilityScope? = null
) {
    BackHandler {
        viewModel.navigateTo(AppScreen.DASHBOARD)
    }

    val habits by viewModel.allHabits.collectAsState()
    val allLogs by viewModel.allLogs.collectAsState()
    val habit = habits.firstOrNull { it.id == habitId }

    if (habit == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Text("Habit not found", color = MaterialTheme.colorScheme.onSurface)
        }
        return
    }

    val habitLogs = allLogs.filter { it.habitId == habit.id }
    val notesFlow = remember(habit.id) { viewModel.getNotesForHabit(habit.id) }
    val notes by notesFlow.collectAsState(initial = emptyList())
    val notesDates = remember(notes) { notes.map { it.date }.toSet() }

    val habitColor = Color(habit.colorHex)
    val stats = remember(habit, habitLogs) { viewModel.calculateStats(habit, habitLogs) }

    var selectedCalendarMonth by remember { mutableStateOf(YearMonth.now()) }

    // Dialog state for adding/editing note
    var showNoteDialog by remember { mutableStateOf(false) }
    var noteTargetDate by remember { mutableStateOf(LocalDate.now()) }
    var noteInputText by remember { mutableStateOf("") }
    var editingNoteId by remember { mutableStateOf(0L) }

    // Value input dialog for count/measure
    var showValueDialog by remember { mutableStateOf(false) }
    var valueTargetDate by remember { mutableStateOf(LocalDate.now()) }
    var valueInputText by remember { mutableStateOf("") }

    // Permanent delete confirmation (archived habits)
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // Shake feedback for non-numeric log amounts
    val valueShake = rememberShakeController()
    val shakeScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            // Content flows behind the transparent nav bar (no dead band);
            // bottom padding keeps the last item clear of the gesture pill.
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top App Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircleIconButton(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) },
                        testTag = "detail_back_button"
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Reminders button
                        CircleIconButton(
                            imageVector = Icons.Filled.NotificationsActive,
                            contentDescription = "Reminders",
                            onClick = { viewModel.navigateTo(AppScreen.REMINDERS, habit.id) },
                            iconSize = 18.dp,
                            testTag = "reminders_button"
                        )

                        // Notes button
                        CircleIconButton(
                            imageVector = Icons.Filled.EditNote,
                            contentDescription = "Notes",
                            onClick = { viewModel.navigateTo(AppScreen.NOTES, habit.id) },
                            testTag = "notes_screen_button"
                        )

                        // Archive / Restore button (icon morphs on toggle)
                        AnimatedContent(
                            targetState = habit.isArchived,
                            label = "archive_icon_morph"
                        ) { archived ->
                            CircleIconButton(
                                imageVector = if (archived) Icons.Filled.Unarchive else Icons.Filled.Archive,
                                contentDescription = if (archived) "Restore Habit" else "Archive Habit",
                                onClick = { viewModel.toggleArchiveHabit(habit) },
                                iconSize = 18.dp,
                                contentColor = if (archived) Color(0xFFFCC419) else MaterialTheme.colorScheme.onSurface,
                                testTag = "archive_habit_button"
                            )
                        }

                        // Edit button
                        CircleIconButton(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit Habit",
                            onClick = { viewModel.startEditHabit(habit) },
                            iconSize = 18.dp,
                            testTag = "edit_habit_button"
                        )
                    }
                }
            }

            // Habit Header (Icon badge + Title + Subtitle)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .habitIconTransition(habit.id, sharedScope, animatedScope)
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(habitColor.copy(alpha = 0.16f))
                            .border(1.dp, habitColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = HabitIconCatalog.getIcon(habit.iconName),
                            contentDescription = habit.name,
                            tint = habitColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = habit.name,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        if (habit.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = habit.description,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    // Archived habits: no complete action — restore or delete.
                    if (habit.isArchived) {
                        Spacer(modifier = Modifier.weight(1f))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CircleIconButton(
                                imageVector = Icons.Filled.Unarchive,
                                contentDescription = "Restore habit",
                                onClick = { viewModel.toggleArchiveHabit(habit) },
                                size = 44.dp,
                                iconSize = 20.dp,
                                testTag = "detail_unarchive_button"
                            )
                            CircleIconButton(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete habit",
                                onClick = { showDeleteConfirm = true },
                                size = 44.dp,
                                iconSize = 20.dp,
                                containerColor = LocalAppChrome.current.dangerContainer,
                                contentColor = HabitCoral,
                                testTag = "detail_delete_button"
                            )
                        }
                    }
                }
            }

            // Contribution Grid Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(22.dp))
                        .padding(16.dp)
                ) {
                    ContributionGrid(
                        habit = habit,
                        logs = habitLogs,
                        columns = 20,
                        rows = 7,
                        showLabels = true,
                        tileHeight = 9.dp,
                        onDateClick = { date ->
                            if (habit.trackingType == TrackingType.YES_NO) {
                                viewModel.toggleHabitDay(habit, date)
                            } else {
                                valueTargetDate = date
                                val existingLog = habitLogs.firstOrNull { it.date == date.format(DateTimeFormatter.ISO_LOCAL_DATE) }
                                valueInputText = (existingLog?.value ?: habit.targetValue).toString()
                                showValueDialog = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Streak & Info Badges Row (streak ticks up/down on change)
            item {
                val animatedStreak by animateIntAsState(
                    targetValue = stats.currentStreak,
                    animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
                    label = "streak_tick"
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Current Streak Pill
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(LocalAppChrome.current.containerAlt)
                            .border(1.dp, habitColor.copy(alpha = 0.3f), CircleShape)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = habitColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$animatedStreak",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = habitColor
                            )
                        )
                    }

                    // Frequency Pill
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(LocalAppChrome.current.containerAlt)
                            .border(1.dp, LocalAppChrome.current.border, CircleShape)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = "Frequency",
                            tint = habitColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (habit.frequencyDaysPerWeek == 7) "Daily" else "${habit.frequencyDaysPerWeek}x / week",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }

                    // Best Streak Pill
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(LocalAppChrome.current.containerAlt)
                            .border(1.dp, LocalAppChrome.current.border, CircleShape)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Best: ${stats.bestStreak}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            // Month Calendar Card
            item {
                MonthCalendarView(
                    habit = habit,
                    currentYearMonth = selectedCalendarMonth,
                    logs = habitLogs,
                    notesDates = notesDates,
                    onPreviousMonth = {
                        selectedCalendarMonth = selectedCalendarMonth.minusMonths(1)
                    },
                    onNextMonth = {
                        selectedCalendarMonth = selectedCalendarMonth.plusMonths(1)
                    },
                    onDayClick = { date ->
                        if (habit.trackingType == TrackingType.YES_NO) {
                            viewModel.toggleHabitDay(habit, date)
                        } else {
                            valueTargetDate = date
                            val existingLog = habitLogs.firstOrNull { it.date == date.format(DateTimeFormatter.ISO_LOCAL_DATE) }
                            valueInputText = (existingLog?.value ?: habit.targetValue).toString()
                            showValueDialog = true
                        }
                    },
                    onDayLongClick = { date ->
                        noteTargetDate = date
                        val existing = notes.firstOrNull { it.date == date.format(DateTimeFormatter.ISO_LOCAL_DATE) }
                        if (existing != null) {
                            editingNoteId = existing.id
                            noteInputText = existing.content
                        } else {
                            editingNoteId = 0L
                            noteInputText = ""
                        }
                        showNoteDialog = true
                    }
                )
            }

            // Notes Preview Card (Screenshot 3 & 6)
            item {
                val latestNote = notes.firstOrNull()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(22.dp))
                        .clickable { viewModel.navigateTo(AppScreen.NOTES, habit.id) }
                        .padding(16.dp)
                        .testTag("notes_preview_card")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(habitColor.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.EditNote,
                                contentDescription = "Notes",
                                tint = habitColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            if (latestNote != null) {
                                Text(
                                    text = "Notes (${notes.size})",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = latestNote.content,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    maxLines = 1
                                )
                            } else {
                                Text(
                                    text = "No notes yet",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "What went well? What got in the way?",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                noteTargetDate = LocalDate.now()
                                editingNoteId = 0L
                                noteInputText = ""
                                showNoteDialog = true
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(habitColor.copy(alpha = 0.18f))
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add Note",
                                tint = habitColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Add / Edit Note Dialog
    if (showNoteDialog) {
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = {
                Text(
                    text = if (editingNoteId == 0L) "Add Note" else "Edit Note",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Date: ${noteTargetDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = noteInputText,
                        onValueChange = { noteInputText = it },
                        placeholder = { Text("What went well? What got in the way?") },
                        minLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("note_input_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = LocalAppChrome.current.container,
                            unfocusedContainerColor = LocalAppChrome.current.container,
                            focusedBorderColor = habitColor,
                            unfocusedBorderColor = LocalAppChrome.current.border
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noteInputText.isNotBlank()) {
                            viewModel.saveNote(
                                habitId = habit.id,
                                date = noteTargetDate,
                                content = noteInputText,
                                noteId = editingNoteId
                            )
                        }
                        showNoteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                ) {
                    Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoteDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // Set Value Dialog (for Count/Measure)
    if (showValueDialog) {
        AlertDialog(
            onDismissRequest = { showValueDialog = false },
            title = {
                Text(
                    text = "Log Amount",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Date: ${valueTargetDate.format(DateTimeFormatter.ISO_LOCAL_DATE)} (${habit.unit})",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = valueInputText,
                        onValueChange = { valueInputText = it },
                        placeholder = { Text("e.g. ${habit.targetValue}") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .shake(valueShake)
                            .testTag("value_input_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = LocalAppChrome.current.container,
                            unfocusedContainerColor = LocalAppChrome.current.container,
                            focusedBorderColor = habitColor,
                            unfocusedBorderColor = LocalAppChrome.current.border
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = valueInputText.toFloatOrNull()
                        if (num == null) {
                            shakeScope.launch { valueShake.shake() }
                        } else {
                            viewModel.setHabitDayValue(habit, valueTargetDate, num)
                            showValueDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                ) {
                    Text("Set", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showValueDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // Permanent delete confirmation
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Text(
                    text = "Delete \"${habit.name}\"?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "This permanently removes the habit with all its logs and notes. This cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteHabit(habit)
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HabitCoral),
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}
