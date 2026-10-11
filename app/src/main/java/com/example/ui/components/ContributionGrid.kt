package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Habit
import com.example.data.model.HabitLog
import com.example.data.model.HabitType
import com.example.ui.theme.TileBorderDark
import com.example.ui.theme.TileInactiveDark
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JTextStyle
import java.util.Locale

@Composable
fun ContributionGrid(
    habit: Habit,
    logs: List<HabitLog>,
    modifier: Modifier = Modifier,
    columns: Int = 24,
    rows: Int = 7,
    showLabels: Boolean = false,
    tileHeight: Dp = 10.dp,
    onDateClick: ((LocalDate) -> Unit)? = null
) {
    val completedDatesMap = remember(logs) {
        logs.associate { it.date to it.value }
    }

    val today = remember { LocalDate.now() }
    val formatter = remember { DateTimeFormatter.ISO_LOCAL_DATE }

    // End on current week Sunday (or Saturday) so columns align
    val currentDayOfWeek = today.dayOfWeek.value // 1 = Monday .. 7 = Sunday
    val daysToEndOfWeek = 7 - currentDayOfWeek
    val endDate = today.plusDays(daysToEndOfWeek.toLong())

    val totalDays = columns * rows
    val startDate = endDate.minusDays((totalDays - 1).toLong())

    // Compute month markers for header if showLabels is true
    val monthMarkers = remember(columns, startDate) {
        val markers = mutableListOf<Pair<Int, String>>()
        var lastMonth = -1
        for (col in 0 until columns) {
            val colDate = startDate.plusDays((col * rows).toLong())
            if (colDate.monthValue != lastMonth) {
                lastMonth = colDate.monthValue
                markers.add(
                    col to colDate.month.getDisplayName(
                        JTextStyle.SHORT,
                        Locale.getDefault()
                    )
                )
            }
        }
        markers
    }

    val habitColor = Color(habit.colorHex)
    val inactiveColor = TileInactiveDark
    val borderColor = TileBorderDark

    Column(modifier = modifier) {
        if (showLabels) {
            // Month labels row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 28.dp, bottom = 6.dp)
            ) {
                monthMarkers.forEach { (_, monthName) ->
                    Text(
                        text = monthName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            if (showLabels) {
                // Day of week labels (Tue, Thu, Sat)
                Column(
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .height(tileHeight * rows + (rows - 1).dp * 2.5f)
                ) {
                    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    days.forEachIndexed { index, name ->
                        if (index == 1 || index == 3 || index == 5) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.height(tileHeight + 2.5.dp)
                            )
                        } else {
                            Box(modifier = Modifier.height(tileHeight + 2.5.dp))
                        }
                    }
                }
            }

            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(tileHeight * rows + (rows - 1).dp * 2.5f)
                    .then(
                        if (onDateClick != null) {
                            Modifier.pointerInput(columns, rows) {
                                detectTapGestures { tapOffset ->
                                    val cellWidth = size.width / columns
                                    val cellHeight = size.height / rows
                                    val col = (tapOffset.x / cellWidth).toInt().coerceIn(0, columns - 1)
                                    val row = (tapOffset.y / cellHeight).toInt().coerceIn(0, rows - 1)
                                    val dayIndex = col * rows + row
                                    val clickedDate = startDate.plusDays(dayIndex.toLong())
                                    if (!clickedDate.isAfter(today)) {
                                        onDateClick(clickedDate)
                                    }
                                }
                            }
                        } else Modifier
                    )
            ) {
                val totalWidth = size.width
                val totalHeight = size.height
                val spacing = 3.dp.toPx()

                val cellWidth = (totalWidth - (columns - 1) * spacing) / columns
                val cellHeight = (totalHeight - (rows - 1) * spacing) / rows
                val cornerRadius = CornerRadius(cellWidth * 0.28f, cellHeight * 0.28f)

                for (col in 0 until columns) {
                    for (row in 0 until rows) {
                        val dayIndex = col * rows + row
                        val cellDate = startDate.plusDays(dayIndex.toLong())
                        val isFuture = cellDate.isAfter(today)

                        val x = col * (cellWidth + spacing)
                        val y = row * (cellHeight + spacing)

                        val dateStr = cellDate.format(formatter)
                        val loggedValue = completedDatesMap[dateStr]

                        val isFilled = if (isFuture) {
                            false
                        } else if (habit.habitType == HabitType.QUIT) {
                            // Clean if no slip logged (loggedValue == null or 0)
                            loggedValue == null || loggedValue <= 0f
                        } else {
                            loggedValue != null && loggedValue >= (habit.targetValue * 0.3f)
                        }

                        val fillRatio = if (isFuture || !isFilled) {
                            0f
                        } else if (habit.habitType == HabitType.QUIT) {
                            1f
                        } else {
                            val v = loggedValue ?: 0f
                            (v / habit.targetValue).coerceIn(0.4f, 1f)
                        }

                        // Draw background/inactive tile
                        drawRoundRect(
                            color = if (isFuture) inactiveColor.copy(alpha = 0.3f) else inactiveColor,
                            topLeft = Offset(x, y),
                            size = Size(cellWidth, cellHeight),
                            cornerRadius = cornerRadius
                        )

                        // If filled, paint with habit color
                        if (isFilled) {
                            val tileColor = if (fillRatio >= 1f) habitColor else habitColor.copy(alpha = fillRatio)
                            drawRoundRect(
                                color = tileColor,
                                topLeft = Offset(x, y),
                                size = Size(cellWidth, cellHeight),
                                cornerRadius = cornerRadius
                            )
                        }
                    }
                }
            }
        }
    }
}
