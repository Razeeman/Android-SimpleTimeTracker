package com.example.util.simpletimetracker.feature_change_record_type.viewData

import androidx.annotation.ColorInt

data class ChangeRecordTypeIconImageStateViewData(
    val state: IconImageState,
    val searchButtonIsVisible: Boolean,
    @ColorInt val searchButtonColor: Int,
) {

    sealed interface IconImageState {
        object Chooser : IconImageState
        object Search : IconImageState
    }
}