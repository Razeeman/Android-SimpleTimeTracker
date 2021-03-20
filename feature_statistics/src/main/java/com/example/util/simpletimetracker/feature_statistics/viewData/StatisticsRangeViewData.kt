package com.example.util.simpletimetracker.feature_statistics.viewData

import com.example.util.simpletimetracker.core.view.spinner.CustomSpinner

data class StatisticsRangeViewData(
    val range: RangeLength,
    override val text: String
) : CustomSpinner.CustomSpinnerItem()