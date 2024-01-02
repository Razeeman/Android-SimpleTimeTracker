package com.example.util.simpletimetracker.feature_settings.viewData

import com.example.util.simpletimetracker.feature_views.spinner.CustomSpinner

data class WidgetTransparencyViewData(
    val items: List<CustomSpinner.CustomSpinnerItem>,
    val selectedPosition: Int,
    val selectedValue: String,
)