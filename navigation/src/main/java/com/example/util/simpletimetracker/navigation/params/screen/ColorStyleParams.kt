package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import androidx.annotation.ColorInt
import kotlinx.parcelize.Parcelize

sealed class ColorStyleParams : Parcelable {
    @Parcelize
    data class Solid(
        @ColorInt val color: Int,
    ) : ColorStyleParams()

    @Parcelize
    data class LinearGradient(
        @ColorInt val start: Int,
        @ColorInt val end: Int,
        val angleDegrees: Int,
    ) : ColorStyleParams()
}
