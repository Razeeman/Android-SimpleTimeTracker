package com.example.util.simpletimetracker.feature_change_complex_rule.viewData

import com.example.util.simpletimetracker.core.view.ViewChooserStateDelegate

sealed interface ChangeComplexRuleChooserState : ViewChooserStateDelegate.State {
    object Closed : ChangeComplexRuleChooserState, ViewChooserStateDelegate.State.Closed
    object Action : ChangeComplexRuleChooserState
    object StartingTypes : ChangeComplexRuleChooserState
    object CurrentTypes : ChangeComplexRuleChooserState
    object DayOfWeek : ChangeComplexRuleChooserState
}
