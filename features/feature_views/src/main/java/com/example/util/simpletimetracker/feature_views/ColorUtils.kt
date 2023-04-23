package com.example.util.simpletimetracker.feature_views

import android.graphics.Color
import androidx.annotation.ColorInt

object ColorUtils {

    /**
     * Darkens color.
     */
    @ColorInt
    fun darkenColor(@ColorInt color: Int): Int {
        return FloatArray(3).apply {
            Color.colorToHSV(color, this)
            // change value
            this[2] -= 0.1f
        }.let(Color::HSVToColor)
    }

    /**
     * Lightens dark colors and darkens light colors.
     */
    @ColorInt
    fun normalizeLightness(
        @ColorInt color: Int,
        factor: Float = 0.05f,
    ): Int {
        return FloatArray(3).apply {
            Color.colorToHSV(color, this)
            // change value
            if (this[2] > 0.5f) {
                this[2] -= factor
            } else {
                this[2] += factor
            }
        }.let(Color::HSVToColor)
    }
}