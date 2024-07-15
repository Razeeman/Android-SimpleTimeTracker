package com.example.util.simpletimetracker.feature_dialogs.duration.model

import com.example.util.simpletimetracker.feature_dialogs.duration.customView.DurationView

data class DurationDialogState(
    val isDisableButtonVisible: Boolean,
    val value: Value,
) {

    sealed interface Value {
        data class Duration(val data: DurationView.ViewData) : Value
        data class Count(val data: String) : Value
    }
}