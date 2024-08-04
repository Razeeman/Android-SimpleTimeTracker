package com.example.util.simpletimetracker.feature_records.model

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_records.customView.RecordsCalendarViewData

data class RecordsShareState(
    val shareTitle: String,
    val state: State,
) {

    sealed interface State {
        data class Records(val data: List<ViewHolderType>) : State
        data class Calendar(val data: RecordsCalendarViewData) : State
    }
}