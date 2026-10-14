package com.example.ui.util

import android.graphics.Paint as AndroidPaint
import android.os.Build
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
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
import androidx.compose.foundation.shape.CornerBasedShape
import com.example.ui.theme.LocalAppChrome
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy

val LocalBackdrop = compositionLocalOf<LayerBackdrop?> { null }

/**
 * Frost glass tokens, ported from SpendSense `FrostGlassDefaults`
 * (`presentation/util/ModifierExtensions.kt:69`), tuned for Habit Tracker's
 * dark palette (DarkSurface #16181F instead of GlassSurface #16161F).
 */
object FrostGlassDefaults {
    val containerColor: Color = Color(0xFF16181F).copy(alpha = 0.72f)
    val liveBlurContainerColor: Color = Color(0xFF16181F).copy(alpha = 0.60f)
    val borderWidth: Dp = 1.dp
    const val borderAlpha: Float = 0.22f
    const val sheenAlpha: Float = 0.10f
    const val prismAlpha: Float = 0.04f
}

/**
 * Premium frosted glass effect with real live-blur backdrop sampling via
 * `io.github.kyant0:backdrop`:
 * - Live path (`liveBlur = true` + backdrop available): GPU pipeline
 *   `vibrancy() → blur(7dp) → lens(26dp)` on API 33+, plus frosted tint,
 *   specular sheen and gradient border.
 * - Static path: lightweight tint + sheen + border, zero offscreen buffers
 *   (for list rows / large cards — see Tiered Virtualization: `useLens`
 *   only on ~3–5 small hero elements such as this nav bar).
 *
 * @param backdrop Optional explicit [Backdrop] to sample (defaults to
 * [LocalBackdrop.current]). The sampler must sit OUTSIDE the
 * `layerBackdrop` node it samples, as a later sibling.
 */
@Composable
fun Modifier.glassEffect(
    shape: Shape,
    containerColor: Color = FrostGlassDefaults.containerColor,
    borderWidth: Dp = FrostGlassDefaults.borderWidth,
    borderAlpha: Float = FrostGlassDefaults.borderAlpha,
    sheenAlpha: Float = FrostGlassDefaults.sheenAlpha,
    prismAlpha: Float = FrostGlassDefaults.prismAlpha,
    liveBlur: Boolean = true,
    useLens: Boolean = true,
    backdrop: Backdrop? = null,
    contentModifier: Modifier = Modifier
): Modifier {
    val activeBackdrop = backdrop ?: LocalBackdrop.current
    // Resolve the default frost tint from the current theme so the glass
    // reads correctly in both dark and light mode. Explicit colors pass
    // through unchanged.
    val chrome = LocalAppChrome.current
    val isDefaultContainer = containerColor == FrostGlassDefaults.containerColor
    val effectiveColor = if (liveBlur && activeBackdrop != null) {
        if (isDefaultContainer) {
            chrome.glassLiveContainer
        } else {
            containerColor
        }
    } else {
        if (isDefaultContainer) chrome.glassContainer else containerColor
    }

    return if (liveBlur && activeBackdrop != null) {
        this.drawBackdrop(
            backdrop = activeBackdrop,
            shape = { shape },
            effects = {
                vibrancy()
                blur(7f.dp.toPx())
                // Lens refraction is API 33+ and strictly requires CornerBasedShape.
                if (useLens && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && shape is CornerBasedShape) {
                    try {
                        lens(refractionHeight = 26f.dp.toPx(), refractionAmount = 26f.dp.toPx())
                    } catch (_: Throwable) {
                        // Fall back gracefully to vibrancy + blur without crashing
                    }
                }
            },
            onDrawSurface = {
                if (effectiveColor != Color.Transparent && effectiveColor.alpha > 0f) {
                    drawRect(effectiveColor)
                }
                if (sheenAlpha > 0f) {
                    drawRect(
                        brush = Brush.verticalGradient(
                            0f to Color.White.copy(alpha = sheenAlpha),
                            0.5f to Color.Transparent
                        )
                    )
                }
            }
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
    } else {
        this.clip(shape)
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
