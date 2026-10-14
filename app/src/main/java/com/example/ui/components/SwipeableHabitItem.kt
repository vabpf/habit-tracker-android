package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Swipe actions for home-screen habit items.
 * - Swipe right (start → end): log/unlog today.
 * - Swipe left (end → start): archive / restore.
 *
 * The action fires on release and the item snaps back; nothing is deleted,
 * both actions are reversible from the list itself.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableHabitItem(
    onToggleToday: () -> Unit,
    onArchiveToggle: () -> Unit,
    isArchived: Boolean,
    modifier: Modifier = Modifier,
    backgroundShape: Shape = RoundedCornerShape(22.dp),
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onToggleToday()
                    false // snap back; item stays in the list
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onArchiveToggle()
                    false // snap back (archived items leave via list update)
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            if (direction != null) {
                val (bgColor, icon, alignment) = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> Triple(
                        Color(0xFF43A047),
                        Icons.Filled.Check,
                        Alignment.CenterStart
                    )
                    else -> Triple(
                        Color(0xFFF9A825),
                        if (isArchived) Icons.Filled.Unarchive else Icons.Filled.Archive,
                        Alignment.CenterEnd
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(backgroundShape)
                        .background(bgColor)
                        .padding(horizontal = 20.dp),
                    contentAlignment = alignment
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        modifier = modifier,
        content = { content() }
    )
}
