package com.example.util.simpletimetracker.feature_widget.universal.customView

import androidx.annotation.ColorInt

data class WidgetUniversalViewData(
    val data: List<Pair<Int, Int>>,
    @ColorInt val iconColor: Int
)