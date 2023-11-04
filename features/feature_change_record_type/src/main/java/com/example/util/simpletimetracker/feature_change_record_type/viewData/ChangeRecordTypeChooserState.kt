package com.example.util.simpletimetracker.feature_change_record_type.viewData

data class ChangeRecordTypeChooserState(
    val current: State,
    val previous: State,
) {

    sealed interface State {
        object Closed : State
        object Color : State
        object Icon : State
        object Category : State
        object GoalTime : State
        object Type: State
    }
}
