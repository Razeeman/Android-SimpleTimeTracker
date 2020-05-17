package com.example.util.simpletimetracker.feature_statistics.customView

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes

data class PiePortion(
    val value: Long,
    @ColorInt val colorInt: Int,
    @DrawableRes val iconId: Int? = null
)