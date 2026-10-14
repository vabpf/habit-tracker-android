package com.example.widget

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF

/**
 * Fakes real frosted glass for home-screen widgets.
 *
 * Widgets run as RemoteViews in the launcher process and cannot sample or
 * blur what's behind them (unlike the in-app glass effect, which taps the
 * GPU backdrop pipeline). The workaround: read the system wallpaper,
 * center-crop it to the card's aspect ratio, blur it by down/upscaling,
 * dim it, and round its corners — then set it as a background bitmap.
 *
 * Returns null when the wallpaper is unavailable (e.g. live wallpapers);
 * callers must fall back to the static translucent glass drawable.
 * The heavy blur hides the fact that the crop can't track the widget's
 * exact on-screen position.
 */
object WidgetGlassBackground {

    fun render(
        context: Context,
        targetWidth: Int = 1000,
        targetHeight: Int = 640,
        cornerPx: Float = 64f,
        dimColor: Int = Color.argb(122, 0, 0, 0)
    ): Bitmap? {
        return try {
            val drawable = WallpaperManager.getInstance(context).drawable
                ?: return null
            val dw = drawable.intrinsicWidth
            val dh = drawable.intrinsicHeight
            if (dw <= 0 || dh <= 0) return null

            val targetAspect = targetWidth.toFloat() / targetHeight
            val srcAspect = dw.toFloat() / dh
            val cropW: Int
            val cropH: Int
            if (srcAspect > targetAspect) {
                cropH = dh
                cropW = (dh * targetAspect).toInt().coerceAtLeast(1)
            } else {
                cropW = dw
                cropH = (dw / targetAspect).toInt().coerceAtLeast(1)
            }
            val cx = (dw - cropW) / 2
            val cy = (dh - cropH) / 2

            // Draw the crop tiny — upscaling with filtering IS the blur.
            val smallW = 160
            val smallH = (smallW / targetAspect).toInt().coerceAtLeast(1)
            val small = Bitmap.createBitmap(smallW, smallH, Bitmap.Config.ARGB_8888)
            val smallCanvas = Canvas(small)
            smallCanvas.translate(
                -cx * smallW / cropW.toFloat(),
                -cy * smallH / cropH.toFloat()
            )
            smallCanvas.scale(
                smallW / cropW.toFloat(),
                smallH / cropH.toFloat()
            )
            drawable.setBounds(0, 0, dw, dh)
            drawable.draw(smallCanvas)

            val blurred = Bitmap.createScaledBitmap(small, targetWidth, targetHeight, true)
            small.recycle()

            // Dim + round corners + hairline border, baked into the bitmap.
            val out = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(out)
            val full = RectF(0f, 0f, targetWidth.toFloat(), targetHeight.toFloat())
            val clip = Path().apply {
                addRoundRect(full, cornerPx, cornerPx, Path.Direction.CW)
            }
            canvas.save()
            canvas.clipPath(clip)
            canvas.drawBitmap(
                blurred,
                null,
                full,
                Paint(Paint.FILTER_BITMAP_FLAG)
            )
            canvas.drawColor(dimColor)
            canvas.restore()

            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 2f
                color = Color.argb(56, 255, 255, 255)
            }
            val inset = 1f
            canvas.drawRoundRect(
                RectF(inset, inset, targetWidth - inset, targetHeight - inset),
                cornerPx,
                cornerPx,
                borderPaint
            )
            blurred.recycle()
            out
        } catch (_: Throwable) {
            null
        }
    }
}
