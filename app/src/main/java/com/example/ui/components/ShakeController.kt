package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Horizontal shake for invalid input (blank habit name, non-numeric log
 * amount). Attach via [Modifier.shake], trigger via [ShakeController.shake]
 * from a coroutine scope when validation fails.
 */
class ShakeController {
    internal val offsetX = Animatable(0f)

    suspend fun shake() {
        offsetX.snapTo(0f)
        offsetX.animateTo(
            targetValue = 0f,
            animationSpec = keyframes {
                durationMillis = 400
                -14f at 80
                12f at 160
                -8f at 240
                5f at 320
            }
        )
    }
}

@Composable
fun rememberShakeController(): ShakeController = remember { ShakeController() }

fun Modifier.shake(controller: ShakeController): Modifier =
    graphicsLayer { translationX = controller.offsetX.value }
