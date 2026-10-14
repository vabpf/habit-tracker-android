package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.example.data.model.HabitType
import com.example.ui.theme.LocalAppChrome
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun MonthCalendarView(
    habit: Habit,
    currentYearMonth: YearMonth,
    logs: List<HabitLog>,
    notesDates: Set<String>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDayClick: (LocalDate) -> Unit,
    onDayLongClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val habitColor = Color(habit.colorHex)
    val today = remember { LocalDate.now() }
    val formatter = remember { DateTimeFormatter.ISO_LOCAL_DATE }
    val logsMap = remember(logs) { logs.associateBy { it.date } }

    val daysInMonth = currentYearMonth.lengthOfMonth()
    val firstDayOfMonth = currentYearMonth.atDay(1)
    val dayOfWeekOffset = (firstDayOfMonth.dayOfWeek.value - 1) // 0 = Mon .. 6 = Sun

    // Previous month filler days
    val prevMonth = currentYearMonth.minusMonths(1)
    val daysInPrevMonth = prevMonth.lengthOfMonth()

    val totalCells = (dayOfWeekOffset + daysInMonth + 6) / 7 * 7

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(22.dp)
            )
            .padding(16.dp)
    ) {
        // Weekday Headers: Mon Tue Wed Thu Fri Sat Sun
        val weekDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weekDays.forEach { dayName ->
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Calendar Grid
        val rows = totalCells / 7
        for (r in 0 until rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (c in 0 until 7) {
                    val cellIndex = r * 7 + c
                    val dayNum = cellIndex - dayOfWeekOffset + 1

                    val (cellDate, isCurrentMonth) = when {
                        dayNum < 1 -> {
                            val prevDay = daysInPrevMonth + dayNum
                            prevMonth.atDay(prevDay) to false
                        }
                        dayNum > daysInMonth -> {
                            val nextDay = dayNum - daysInMonth
                            currentYearMonth.plusMonths(1).atDay(nextDay) to false
                        }
                        else -> {
                            currentYearMonth.atDay(dayNum) to true
                        }
                    }

                    val dateStr = cellDate.format(formatter)
                    val isFuture = cellDate.isAfter(today)
                    val isToday = cellDate == today
                    val log = logsMap[dateStr]

                    val isCompleted = if (isFuture) {
                        false
                    } else if (habit.habitType == HabitType.QUIT) {
                        log == null || log.value <= 0f
                    } else {
                        log?.isCompleted == true
                    }

                    val hasNote = notesDates.contains(dateStr)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when {
                                    !isCurrentMonth -> LocalAppChrome.current.tileDim
                                    isCompleted -> habitColor.copy(alpha = 0.22f)
                                    else -> LocalAppChrome.current.tileInactive
                                }
                            )
                            .border(
                                width = if (isToday) 1.5.dp else 1.dp,
                                color = when {
                                    isToday -> habitColor
                                    isCompleted -> habitColor.copy(alpha = 0.45f)
                                    else -> LocalAppChrome.current.border
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                            .combinedClickable(
                                onClick = {
                                    if (!isFuture) onDayClick(cellDate)
                                },
                                onLongClick = {
                                    if (!isFuture) onDayLongClick(cellDate)
                                }
                            )
                            .testTag("calendar_cell_$dateStr"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = cellDate.dayOfMonth.toString(),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isToday || isCompleted) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize = 13.sp,
                                    color = when {
                                        !isCurrentMonth -> LocalAppChrome.current.muted
                                        isFuture -> LocalAppChrome.current.muted
                                        isCompleted -> MaterialTheme.colorScheme.onSurface
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            )

                            // Status Dot below date
                            if (isCompleted || hasNote) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    if (isCompleted) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(habitColor)
                                        )
                                    }
                                    if (hasNote) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFE599F7))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Month Selector Pill Footer: [icon Month Year]  [<] [>]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(LocalAppChrome.current.container)
                    .border(1.dp, LocalAppChrome.current.border, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                val monthLabel = "${currentYearMonth.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())} ${currentYearMonth.year}"
                Text(
                    text = monthLabel,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(
                    onClick = onPreviousMonth,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(LocalAppChrome.current.container)
                        .border(1.dp, LocalAppChrome.current.border, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous Month",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = onNextMonth,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(LocalAppChrome.current.container)
                        .border(1.dp, LocalAppChrome.current.border, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next Month",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Press and hold a day to add a note",
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}
