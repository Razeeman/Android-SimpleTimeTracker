package com.example.util.simpletimetracker.feature_views.viewData

import androidx.annotation.ColorInt

sealed class ColorStyle {
    @get:ColorInt
    abstract val primaryColor: Int

    data class Solid(
        @get:ColorInt val color: Int,
    ) : ColorStyle() {
        override val primaryColor: Int get() = color
    }

    data class LinearGradient(
        @get:ColorInt val start: Int,
        @get:ColorInt val end: Int,
        // 0 degrees is left to right, 90 degrees is bottom to top.
        val angleDegrees: Int,
    ) : ColorStyle() {
        override val primaryColor: Int get() = start
    }
}