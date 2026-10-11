package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.Habit
import com.example.data.model.HabitType
import com.example.data.model.TrackingType

@Composable
fun HabitActionButton(
    habit: Habit,
    currentValue: Float,
    isCompleted: Boolean,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val habitColor = Color(habit.colorHex)
    val progress = if (habit.targetValue > 0) (currentValue / habit.targetValue).coerceIn(0f, 1f) else 0f

    val scale by animateFloatAsState(
        targetValue = if (isCompleted) 1.05f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "btnScale"
    )

    when (habit.trackingType) {
        TrackingType.YES_NO -> {
            val bgColor by animateColorAsState(
                targetValue = if (isCompleted) habitColor else Color(0xFF1E2129),
                label = "bgColor"
            )
            val iconColor by animateColorAsState(
                targetValue = if (isCompleted) Color(0xFF0F1014) else Color(0xFF5A606E),
                label = "iconColor"
            )

            Box(
                modifier = modifier
                    .size(44.dp)
                    .scale(scale)
                    .clip(RoundedCornerShape(14.dp))
                    .background(bgColor)
                    .border(
                        width = 1.dp,
                        color = if (isCompleted) habitColor else Color(0xFF2C303B),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable { onActionClick() }
                    .testTag("action_toggle_${habit.id}"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = if (isCompleted) "Completed" else "Mark Complete",
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        TrackingType.COUNT, TrackingType.MEASURE -> {
            Box(
                modifier = modifier
                    .size(44.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .clickable { onActionClick() }
                    .testTag("action_increment_${habit.id}"),
                contentAlignment = Alignment.Center
            ) {
                // Background Track
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(42.dp),
                    color = Color(0xFF232631),
                    strokeWidth = 3.5.dp,
                    trackColor = Color.Transparent
                )

                // Foreground Progress Arc
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(42.dp),
                    color = habitColor,
                    strokeWidth = 3.5.dp,
                    trackColor = Color.Transparent
                )

                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Completed",
                        tint = habitColor,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Increment",
                        tint = habitColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
