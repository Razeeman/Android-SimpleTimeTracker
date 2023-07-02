package com.example.util.simpletimetracker.feature_settings.viewData

import com.example.util.simpletimetracker.feature_views.spinner.CustomSpinner

data class LanguageViewData(
    val currentLanguageName: String,
    val items: List<CustomSpinner.CustomSpinnerItem>,
)