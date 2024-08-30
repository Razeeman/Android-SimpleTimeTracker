package com.example.util.simpletimetracker.feature_change_activity_filter.viewData

import com.example.util.simpletimetracker.core.view.ViewChooserStateDelegate

sealed interface ChangeActivityFilterChooserState : ViewChooserStateDelegate.State {
    object Closed : ChangeActivityFilterChooserState, ViewChooserStateDelegate.State.Closed
    object Color : ChangeActivityFilterChooserState
    object Type : ChangeActivityFilterChooserState
}
