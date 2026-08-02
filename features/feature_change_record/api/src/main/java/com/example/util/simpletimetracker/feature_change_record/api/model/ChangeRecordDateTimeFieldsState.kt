package com.example.util.simpletimetracker.feature_change_record.api.model

data class ChangeRecordDateTimeFieldsState(
    val start: State,
    val end: State,
) {

    sealed interface State {
        object DateTime : State
        object Duration : State
    }
}