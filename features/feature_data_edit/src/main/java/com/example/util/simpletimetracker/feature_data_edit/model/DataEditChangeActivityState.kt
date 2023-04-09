package com.example.util.simpletimetracker.feature_data_edit.model

import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData

sealed interface DataEditChangeActivityState {

    object Disabled : DataEditChangeActivityState
    data class Enabled(val viewData: RecordTypeViewData) : DataEditChangeActivityState
}