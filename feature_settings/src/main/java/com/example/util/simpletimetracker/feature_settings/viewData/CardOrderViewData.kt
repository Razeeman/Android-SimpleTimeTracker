package com.example.util.simpletimetracker.feature_settings.viewData

data class CardOrderViewData(
    val items: List<String>,
    val selectedPosition: Int,
    val isManualConfigButtonVisible: Boolean
)