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
    fun normalizeLightness(@ColorInt color: Int): Int {
        val colorNormalization = 0.05f
        return FloatArray(3).apply {
            Color.colorToHSV(color, this)
            // change value
            if (this[2] > 0.5f) {
                this[2] -= colorNormalization
            } else {
                this[2] += colorNormalization
            }
        }.let(Color::HSVToColor)
    }
}