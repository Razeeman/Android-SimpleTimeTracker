package com.example.util.simpletimetracker.feature_records_filter.model

import com.example.util.simpletimetracker.feature_base_adapter.recordFilter.RecordFilterViewData

sealed interface RecordsFilterSelectionState {

    object Hidden : RecordsFilterSelectionState

    data class Visible(val type: RecordFilterViewData.Type) : RecordsFilterSelectionState
}

val RecordsFilterSelectionState.type: RecordFilterViewData.Type?
    get() = (this as? RecordsFilterSelectionState.Visible)?.type