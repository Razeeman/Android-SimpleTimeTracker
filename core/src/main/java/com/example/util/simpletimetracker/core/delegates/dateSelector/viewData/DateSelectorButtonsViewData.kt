package com.example.util.simpletimetracker.core.delegates.dateSelector.viewData

data class DateSelectorButtonsViewData(
    val addButton: Button,
    val optionsButton: Button,
) {

    sealed interface Button {
        data object Hidden : Button
        data class Visible(val iconResId: Int) : Button
    }
}