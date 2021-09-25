package com.example.util.simpletimetracker.core.viewData

import com.example.util.simpletimetracker.feature_views.spinner.CustomSpinner

data class SelectDateViewData(
    override val text: String
) : CustomSpinner.CustomSpinnerItem()