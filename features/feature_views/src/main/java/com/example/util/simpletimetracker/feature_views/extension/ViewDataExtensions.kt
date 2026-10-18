package com.example.util.simpletimetracker.feature_views.extension

import com.example.util.simpletimetracker.feature_views.drawable.AngleGradientDrawable
import com.example.util.simpletimetracker.feature_views.viewData.ColorStyle

fun ColorStyle.LinearGradient.toData(): AngleGradientDrawable.Data {
    return AngleGradientDrawable.Data(
        startColor = this.start,
        endColor = this.end,
        angleDegrees = this.angleDegrees.toFloat(),
    )
}