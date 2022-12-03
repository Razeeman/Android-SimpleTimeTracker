package com.example.util.simpletimetracker.feature_change_record.viewData

data class ChangeRecordChooserState(
    val current: State,
    val previous: State,
) {

    sealed interface State {
        object Closed : State
        object Activity : State
        object Tag : State
        object Comment : State
        object Action : State
    }
}
