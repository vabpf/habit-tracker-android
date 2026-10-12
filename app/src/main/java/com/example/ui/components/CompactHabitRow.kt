package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Habit
import com.example.data.model.HabitLog
import com.example.data.model.HabitType
import com.example.data.model.TrackingType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun CompactHabitRow(
    habit: Habit,
    days: List<LocalDate>,
    logsMap: Map<String, HabitLog>,
    onDayClick: (LocalDate) -> Unit,
    onRowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val habitColor = Color(habit.colorHex)
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onRowClick() }
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Badge
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(habitColor.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = HabitIconCatalog.getIcon(habit.iconName),
                contentDescription = habit.name,
                tint = habitColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Habit Name
        Text(
            text = habit.name,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 14.5.sp,
                color = MaterialTheme.colorScheme.onSurface
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // 5 Days Status Pills
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            days.forEach { date ->
                val dateStr = date.format(formatter)
                val log = logsMap[dateStr]
                val isCompleted = if (habit.habitType == HabitType.QUIT) {
                    log == null || log.value <= 0f
                } else {
                    log?.isCompleted == true
                }
                val currentValue = log?.value ?: 0f
                val progress = if (habit.targetValue > 0) (currentValue / habit.targetValue).coerceIn(0f, 1f) else 0f

                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isCompleted) habitColor
                            else Color(0xFF16181F)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isCompleted) habitColor else Color(0xFF282C36),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onDayClick(date) }
                        .testTag("compact_day_${habit.id}_$dateStr"),
                    contentAlignment = Alignment.Center
                ) {
                    if (!isCompleted && progress > 0f && (habit.trackingType == TrackingType.COUNT || habit.trackingType == TrackingType.MEASURE)) {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.size(18.dp),
                            color = habitColor,
                            strokeWidth = 2.5.dp,
                            trackColor = Color.Transparent
                        )
                    }
                }
            }
        }
    }
}
