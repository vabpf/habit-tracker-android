package com.example.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.RectF
import androidx.annotation.ColorInt

/**
 * Per-widget-instance appearance (background on/off, color, opacity).
 * Stored in SharedPreferences keyed by appWidgetId so every placed
 * widget can look different. Rendered to a bitmap because RemoteViews
 * cannot build shapes at runtime.
 */
data class WidgetAppearance(
    val backgroundEnabled: Boolean = true,
    @ColorInt val backgroundColor: Int = DEFAULT_COLOR,
    val backgroundOpacity: Int = DEFAULT_OPACITY
) {
    companion object {
        const val DEFAULT_OPACITY = 85
        val DEFAULT_COLOR = 0xFF16181FL.toInt()

        /** Preset swatches offered in the editor (dark-first for readability). */
        val SWATCHES = listOf(
            0xFF000000L.toInt(),
            0xFF16181FL.toInt(),
            0xFF1E212AL.toInt(),
            0xFF1B2A4EL.toInt(),
            0xFF2A1E26L.toInt()
        )
    }
}

object WidgetAppearanceStore {
    private const val PREFS = "habit_widget_appearance"

    fun load(context: Context, appWidgetId: Int): WidgetAppearance {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return WidgetAppearance(
            backgroundEnabled = prefs.getBoolean("bg_on_$appWidgetId", true),
            backgroundColor = prefs.getInt(
                "bg_color_$appWidgetId",
                WidgetAppearance.DEFAULT_COLOR
            ),
            backgroundOpacity = prefs.getInt(
                "bg_op_$appWidgetId",
                WidgetAppearance.DEFAULT_OPACITY
            ).coerceIn(0, 100)
        )
    }

    fun save(context: Context, appWidgetId: Int, appearance: WidgetAppearance) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean("bg_on_$appWidgetId", appearance.backgroundEnabled)
            .putInt("bg_color_$appWidgetId", appearance.backgroundColor)
            .putInt(
                "bg_op_$appWidgetId",
                appearance.backgroundOpacity.coerceIn(0, 100)
            )
            .apply()
    }

    fun remove(context: Context, appWidgetId: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .remove("bg_on_$appWidgetId")
            .remove("bg_color_$appWidgetId")
            .remove("bg_op_$appWidgetId")
            .apply()
    }
}

/**
 * Renders the card background bitmap for [appearance]: rounded rect
 * filled with color × opacity plus a hairline light border. Returns null
 * when the background is disabled (callers show a transparent root).
 */
object WidgetCardBackground {
    fun render(
        appearance: WidgetAppearance,
        targetWidth: Int = 1000,
        targetHeight: Int = 640,
        cornerPx: Float = 64f
    ): Bitmap? {
        if (!appearance.backgroundEnabled) return null
        val alpha = (255 * appearance.backgroundOpacity.coerceIn(0, 100) / 100f).toInt()
        val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val base = appearance.backgroundColor
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = base
            this.alpha = alpha
        }
        val full = RectF(0f, 0f, targetWidth.toFloat(), targetHeight.toFloat())
        canvas.drawRoundRect(full, cornerPx, cornerPx, fillPaint)
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = AndroidColor.argb(56, 255, 255, 255)
        }
        val inset = 1f
        canvas.drawRoundRect(
            RectF(inset, inset, targetWidth - inset, targetHeight - inset),
            cornerPx,
            cornerPx,
            borderPaint
        )
        return bitmap
    }
}
