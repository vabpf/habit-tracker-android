package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Subtle enter motion for lazy-list items (fade + rise) built only on
 * bedrock animation APIs, so it compiles on older Compose toolchains
 * where animateItem/animateEnterExit don't exist yet.
 */
@Composable
fun Modifier.listItemEnter(): Modifier {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val progress by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = tween(300)
    )
    return this.graphicsLayer {
        alpha = progress
        translationY = (1f - progress) * 48f
    }
}
