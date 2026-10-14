package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.ui.components.HabitIconCatalog
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.HabitAmber
import com.example.ui.theme.HabitCoral
import com.example.ui.theme.HabitCyan
import com.example.ui.theme.HabitDeepTeal
import com.example.ui.theme.HabitLavender
import com.example.ui.theme.HabitLime
import com.example.ui.theme.HabitMint
import com.example.ui.theme.HabitOrange
import com.example.ui.theme.HabitPeriwinkle
import com.example.ui.theme.HabitPink
import com.example.ui.theme.HabitPurple
import com.example.ui.theme.HabitRose
import com.example.ui.theme.HabitSand
import com.example.ui.theme.HabitSky
import com.example.ui.theme.HabitSlate
import com.example.ui.theme.HabitYellow
import com.example.ui.theme.LocalAppChrome
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.HabitViewModel

@Composable
fun EditHabitScreen(
    viewModel: HabitViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(AppScreen.DASHBOARD)
    }

    val editingHabit by viewModel.editingHabit.collectAsState()

    var name by remember(editingHabit) { mutableStateOf(editingHabit?.name ?: "") }
    var description by remember(editingHabit) { mutableStateOf(editingHabit?.description ?: "") }
    var iconName by remember(editingHabit) { mutableStateOf(editingHabit?.iconName ?: "water") }
    var colorHex by remember(editingHabit) { mutableLongStateOf(editingHabit?.colorHex ?: 0xFF26D3EB) }
    var habitType by remember(editingHabit) { mutableStateOf(editingHabit?.habitType ?: HabitType.BUILD) }
    var trackingType by remember(editingHabit) { mutableStateOf(editingHabit?.trackingType ?: TrackingType.YES_NO) }
    var targetValueStr by remember(editingHabit) { mutableStateOf(editingHabit?.targetValue?.toString() ?: "1") }
    var unit by remember(editingHabit) { mutableStateOf(editingHabit?.unit ?: "") }
    var frequencyDays by remember(editingHabit) { mutableIntStateOf(editingHabit?.frequencyDaysPerWeek ?: 7) }
    var category by remember(editingHabit) { mutableStateOf(editingHabit?.category ?: "Health") }

    val currentColor = Color(colorHex)

    // Palette matching Screenshot 8
    val colorPalette = listOf(
        0xFFFA5252, // Coral
        0xFFFD7E14, // Orange
        0xFFF7BA32, // Amber
        0xFFFCC419, // Yellow
        0xFF8AE034, // Lime
        0xFF3BD9A5, // Mint
        0xFF20C997, // Teal
        0xFF26D3EB, // Cyan
        0xFF339AF0, // Sky
        0xFF748FFC, // Periwinkle
        0xFFA28BF7, // Purple
        0xFFD0BFFF, // Lavender
        0xFFF06595, // Pink
        0xFFFF6B81, // Rose
        0xFF9099A2, // Slate
        0xFFD4A373  // Sand
    )

    val availableIcons = HabitIconCatalog.icons.keys.toList()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Top Bar: [X]  Edit Habit / New Habit
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(LocalAppChrome.current.container)
                            .testTag("edit_habit_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = if (editingHabit == null) "New Habit" else "Edit Habit",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    if (editingHabit != null) {
                        IconButton(
                            onClick = {
                                editingHabit?.let { viewModel.deleteHabit(it) }
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(LocalAppChrome.current.dangerContainer)
                                .testTag("delete_habit_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete Habit",
                                tint = HabitCoral,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        Box(modifier = Modifier.size(38.dp))
                    }
                }
            }

            // Prominent Selected Icon Badge (Screenshot 8)
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(currentColor.copy(alpha = 0.18f))
                            .border(2.dp, currentColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = HabitIconCatalog.getIcon(iconName),
                            contentDescription = null,
                            tint = currentColor,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }
            }

            // Icons Selector Row / Grid
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(availableIcons) { key ->
                        val isSelected = key == iconName
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) currentColor.copy(alpha = 0.25f) else LocalAppChrome.current.chip)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) currentColor else LocalAppChrome.current.border,
                                    shape = CircleShape
                                )
                                .clickable { iconName = key },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = HabitIconCatalog.getIcon(key),
                                contentDescription = key,
                                tint = if (isSelected) currentColor else LocalAppChrome.current.muted,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // NAME Input
            item {
                Column {
                    Text(
                        text = "NAME",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text("e.g. Drink Water") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("habit_name_input"),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = currentColor,
                            unfocusedBorderColor = LocalAppChrome.current.border
                        )
                    )
                }
            }

            // DESCRIPTION Input
            item {
                Column {
                    Text(
                        text = "DESCRIPTION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("e.g. Stay hydrated throughout the day") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("habit_description_input"),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = currentColor,
                            unfocusedBorderColor = LocalAppChrome.current.border
                        )
                    )
                }
            }

            // COLOR Palette Picker (Screenshot 8)
            item {
                Column {
                    Text(
                        text = "COLOR",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // 2 rows of 8 colors
                    val chunked = colorPalette.chunked(8)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        chunked.forEach { rowColors ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                rowColors.forEach { hex ->
                                    val isSelected = hex == colorHex
                                    val col = Color(hex)
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(col)
                                            .border(
                                                width = if (isSelected) 3.dp else 0.dp,
                                                color = if (isSelected) Color.White else Color.Transparent,
                                                shape = CircleShape
                                            )
                                            .clickable { colorHex = hex }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // HABIT TYPE Segmented Button (Build A Habit vs Quit A Habit)
            item {
                Column {
                    Text(
                        text = "HABIT TYPE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(LocalAppChrome.current.chip)
                            .border(1.dp, LocalAppChrome.current.border, RoundedCornerShape(16.dp))
                            .padding(4.dp)
                    ) {
                        // Build A Habit
                        val isBuild = habitType == HabitType.BUILD
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isBuild) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
                                .clickable { habitType = HabitType.BUILD }
                                .padding(vertical = 10.dp)
                                .testTag("type_build_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Build A Habit",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isBuild) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isBuild) MaterialTheme.colorScheme.onSurface else LocalAppChrome.current.muted
                                )
                            )
                        }

                        // Quit A Habit
                        val isQuit = habitType == HabitType.QUIT
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isQuit) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
                                .clickable { habitType = HabitType.QUIT }
                                .padding(vertical = 10.dp)
                                .testTag("type_quit_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Quit A Habit",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isQuit) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isQuit) MaterialTheme.colorScheme.onSurface else LocalAppChrome.current.muted
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (habitType == HabitType.BUILD) "Mark each day you complete it." else "Counts as clean until you log a slip.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // TRACKING Segmented Button (Yes / No vs Count vs Measure)
            item {
                Column {
                    Text(
                        text = "TRACKING",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(LocalAppChrome.current.chip)
                            .border(1.dp, LocalAppChrome.current.border, RoundedCornerShape(16.dp))
                            .padding(4.dp)
                    ) {
                        TrackingType.entries.forEach { t ->
                            val isSelected = trackingType == t
                            val label = when (t) {
                                TrackingType.YES_NO -> "Yes / No"
                                TrackingType.COUNT -> "Count"
                                TrackingType.MEASURE -> "Measure"
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
                                    .clickable { trackingType = t }
                                    .padding(vertical = 10.dp)
                                    .testTag("tracking_mode_${t.name}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else LocalAppChrome.current.muted
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when (trackingType) {
                            TrackingType.YES_NO -> "Simple completion checkmark."
                            TrackingType.COUNT -> "Count reps like pages, glasses, or push-ups."
                            TrackingType.MEASURE -> "Log amounts with a unit — 2 L of water, 7.5 km."
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )

                    if (trackingType != TrackingType.YES_NO) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = targetValueStr,
                                onValueChange = { targetValueStr = it },
                                label = { Text("Daily Target") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                )
                            )

                            OutlinedTextField(
                                value = unit,
                                onValueChange = { unit = it },
                                label = { Text("Unit (L, km, reps)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                        }
                    }
                }
            }

            // CATEGORY Selector
            item {
                Column {
                    Text(
                        text = "CATEGORY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val categories = listOf("Fitness", "Health", "Mind", "Hobbies", "General")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { cat ->
                            val isSelected = category == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) currentColor.copy(alpha = 0.2f) else LocalAppChrome.current.chip)
                                    .border(1.dp, if (isSelected) currentColor else LocalAppChrome.current.border, RoundedCornerShape(12.dp))
                                    .clickable { category = cat }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = cat,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = if (isSelected) currentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Save Button (Screenshot 8)
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            val habitToSave = editingHabit?.copy(
                                name = name,
                                description = description,
                                iconName = iconName,
                                colorHex = colorHex,
                                habitType = habitType,
                                trackingType = trackingType,
                                targetValue = targetValueStr.toFloatOrNull() ?: 1.0f,
                                unit = unit,
                                frequencyDaysPerWeek = frequencyDays,
                                category = category
                            ) ?: Habit(
                                name = name,
                                description = description,
                                iconName = iconName,
                                colorHex = colorHex,
                                habitType = habitType,
                                trackingType = trackingType,
                                targetValue = targetValueStr.toFloatOrNull() ?: 1.0f,
                                unit = unit,
                                frequencyDaysPerWeek = frequencyDays,
                                category = category
                            )
                            viewModel.saveHabit(habitToSave)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("save_habit_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                ) {
                    Text(
                        text = "Save",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
