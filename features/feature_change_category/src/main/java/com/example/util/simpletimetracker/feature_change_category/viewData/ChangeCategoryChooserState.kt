package com.example.util.simpletimetracker.feature_change_category.viewData

import com.example.util.simpletimetracker.core.view.ViewChooserStateDelegate

sealed interface ChangeCategoryChooserState : ViewChooserStateDelegate.State {
    object Closed : ChangeCategoryChooserState, ViewChooserStateDelegate.State.Closed
    object Color : ChangeCategoryChooserState
    object GoalTime : ChangeCategoryChooserState
    object Type : ChangeCategoryChooserState
}
