package com.example.util.simpletimetracker.feature_records_all.viewData

import com.example.util.simpletimetracker.core.view.spinner.CustomSpinner

data class RecordsAllSortOrderViewData(
    val items: List<CustomSpinner.CustomSpinnerItem>,
    val selectedPosition: Int
)