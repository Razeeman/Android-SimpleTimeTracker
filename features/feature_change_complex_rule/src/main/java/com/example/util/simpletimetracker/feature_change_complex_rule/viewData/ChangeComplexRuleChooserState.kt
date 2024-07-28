package com.example.util.simpletimetracker.feature_change_complex_rule.viewData

data class ChangeComplexRuleChooserState(
    val current: State,
    val previous: State,
) {

    sealed interface State {
        object Closed : State
        object Action : State
        object StartingTypes : State
        object CurrentTypes : State
        object DayOfWeek : State
    }
}
