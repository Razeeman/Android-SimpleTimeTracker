package com.example.util.simpletimetracker.feature_settings.viewData

import com.example.util.simpletimetracker.core.view.spinner.CustomSpinner

data class FirstDayOfWeekViewData(
    val items: List<CustomSpinner.CustomSpinnerItem>,
    val selectedPosition: Int
)