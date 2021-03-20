package com.example.util.simpletimetracker.feature_statistics.viewData

import com.example.util.simpletimetracker.core.view.spinner.CustomSpinner

data class StatisticsRangesViewData(
    val items: List<CustomSpinner.CustomSpinnerItem>,
    val selectedPosition: Int
)