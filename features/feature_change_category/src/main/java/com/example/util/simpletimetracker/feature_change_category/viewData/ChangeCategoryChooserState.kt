package com.example.util.simpletimetracker.feature_change_category.viewData

data class ChangeCategoryChooserState(
    val current: State,
    val previous: State,
) {

    sealed interface State {
        object Closed : State
        object Color : State
        object GoalTime : State
        object Type : State
    }
}
