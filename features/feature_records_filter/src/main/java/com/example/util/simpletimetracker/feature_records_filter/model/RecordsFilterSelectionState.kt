package com.example.util.simpletimetracker.feature_records_filter.model

sealed interface RecordsFilterSelectionState {

    object Hidden : RecordsFilterSelectionState

    data class Visible(val type: RecordFilterType) : RecordsFilterSelectionState
}

val RecordsFilterSelectionState.type: RecordFilterType?
    get() = (this as? RecordsFilterSelectionState.Visible)?.type