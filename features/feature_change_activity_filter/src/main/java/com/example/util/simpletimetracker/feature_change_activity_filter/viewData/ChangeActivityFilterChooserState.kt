package com.example.util.simpletimetracker.feature_change_activity_filter.viewData

data class ChangeActivityFilterChooserState(
    val current: State,
    val previous: State,
) {

    sealed interface State {
        object Closed : State
        object Color : State
        object Type : State
    }
}
