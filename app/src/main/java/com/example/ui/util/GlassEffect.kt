package com.example.ui.util

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import android.graphics.Paint as AndroidPaint

/**
 * Frost glass tokens, ported from SpendSense `FrostGlassDefaults`
 * (`presentation/util/ModifierExtensions.kt:69`), tuned for Habit Tracker's
 * dark palette (DarkSurface #16181F instead of GlassSurface #16161F).
 */
object FrostGlassDefaults {
    val containerColor: Color = Color(0xFF16181F).copy(alpha = 0.72f)
    val liveBlurContainerColor: Color = Color(0xFF16181F).copy(alpha = 0.65f)
    val borderWidth: Dp = 1.dp
    const val borderAlpha: Float = 0.22f
    const val sheenAlpha: Float = 0.10f
    const val prismAlpha: Float = 0.04f
}

/**
 * Static-frost glass effect for Habit Tracker.
 *
 * Same funnel signature as SpendSense `Modifier.glassEffect()` so a future
 * migration to `io.github.kyant0:backdrop` is drop-in. The live-blur pipeline
 * (`vibrancy + blur + lens`) is intentionally NOT wired here: Backdrop 2.0.1
 * requires compileSdk 37 + AGP 9.2, while this app is on compileSdk 36 +
 * AGP 9.1.1 — adding it now would break `assembleRelease` on the
 * `checkAarMetadata` task (same failure documented in SpendSense
 * `docs/backdrop-migration-status.md`). The static path (tint + sheen +
 * gradient border, zero offscreen buffers) is the guideline's recommended
 * fallback and is plenty for a single floating nav bar.
 *
 * @param liveBlur / @param useLens / @param backdrop accepted for
 * source-compat with the SpendSense recipe; currently only select the tint
 * alpha (live = 0.65, static = 0.72). Ignored beyond that.
 */
@Composable
fun Modifier.glassEffect(
    shape: Shape,
    containerColor: Color = FrostGlassDefaults.containerColor,
    borderWidth: Dp = FrostGlassDefaults.borderWidth,
    borderAlpha: Float = FrostGlassDefaults.borderAlpha,
    sheenAlpha: Float = FrostGlassDefaults.sheenAlpha,
    liveBlur: Boolean = true,
    useLens: Boolean = true,
    backdrop: Any? = null,
    contentModifier: Modifier = Modifier
): Modifier {
    val effectiveColor = if (liveBlur && containerColor == FrostGlassDefaults.containerColor) {
        FrostGlassDefaults.liveBlurContainerColor
    } else {
        containerColor
    }
    return this
        .clip(shape)
        .background(color = effectiveColor)
        .then(
            if (sheenAlpha > 0f) {
                Modifier.drawBehind {
                    drawRect(
                        brush = Brush.verticalGradient(
                            0f to Color.White.copy(alpha = sheenAlpha),
                            0.5f to Color.Transparent
                        )
                    )
                }
            } else Modifier
        )
        .then(contentModifier)
        .border(
            width = borderWidth,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = borderAlpha),
                    Color.White.copy(alpha = borderAlpha * 0.6f),
                    Color.Transparent,
                    Color.White.copy(alpha = borderAlpha * 0.4f)
                )
            ),
            shape = shape
        )
}

fun Modifier.softDropShadow(
    shape: Shape,
    color: Color = Color.Black.copy(alpha = 0.35f),
    blur: Dp = 16.dp,
    offsetY: Dp = 4.dp,
    offsetX: Dp = 0.dp
): Modifier = this.drawBehind {
    val blurPx = blur.toPx()
    if (blurPx <= 0f) return@drawBehind
    drawIntoCanvas { canvas ->
        val nativePaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            this.color = android.graphics.Color.argb(1, 0, 0, 0)
            setShadowLayer(blurPx, offsetX.toPx(), offsetY.toPx(), color.toArgb())
        }
        when (val outline = shape.createOutline(size, layoutDirection, this)) {
            is Outline.Rectangle -> {
                val r = outline.rect
                canvas.nativeCanvas.drawRect(r.left, r.top, r.right, r.bottom, nativePaint)
            }
            is Outline.Rounded -> {
                val rr = outline.roundRect
                canvas.nativeCanvas.drawRoundRect(
                    rr.left, rr.top, rr.right, rr.bottom,
                    rr.topLeftCornerRadius.x, rr.topLeftCornerRadius.y,
                    nativePaint
                )
            }
            is Outline.Generic -> {
                canvas.nativeCanvas.drawPath(outline.path.asAndroidPath(), nativePaint)
            }
        }
    }
}
