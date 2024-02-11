package com.example.util.simpletimetracker.core.viewData

import com.example.util.simpletimetracker.feature_views.spinner.CustomSpinner

data class RangesViewData(
    val items: List<CustomSpinner.CustomSpinnerItem>,
    val selectedPosition: Int,
)