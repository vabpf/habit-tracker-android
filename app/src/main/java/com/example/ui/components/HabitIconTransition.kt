@file:OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)

package com.example.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.rememberSharedContentState
import androidx.compose.animation.sharedElement
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Shared-element morph for habit icons between list cards and the detail
 * header. Requires both screens to sit under the same SharedTransitionLayout
 * (see MainActivity). No-op when scopes are absent.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.habitIconTransition(
    habitId: Long,
    sharedScope: SharedTransitionScope?,
    animatedScope: AnimatedVisibilityScope?
): Modifier {
    if (sharedScope == null || animatedScope == null) return this
    return with(sharedScope) {
        sharedElement(
            state = rememberSharedContentState(key = "habit-icon-$habitId"),
            animatedVisibilityScope = animatedScope
        )
    }
}
