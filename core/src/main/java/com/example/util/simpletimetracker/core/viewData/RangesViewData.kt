package com.example.util.simpletimetracker.core.viewData

import com.example.util.simpletimetracker.core.view.spinner.CustomSpinner

data class RangesViewData(
    val items: List<CustomSpinner.CustomSpinnerItem>,
    val selectedPosition: Int
)