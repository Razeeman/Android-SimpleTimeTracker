package com.example.util.simpletimetracker.core.viewData

import com.example.util.simpletimetracker.feature_views.spinner.CustomSpinner
import com.example.util.simpletimetracker.domain.model.RangeLength

data class RangeViewData(
    val range: RangeLength,
    override val text: String,
) : CustomSpinner.CustomSpinnerItem()