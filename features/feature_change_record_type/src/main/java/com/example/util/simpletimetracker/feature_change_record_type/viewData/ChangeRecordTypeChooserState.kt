package com.example.util.simpletimetracker.feature_change_record_type.viewData

import com.example.util.simpletimetracker.core.view.ViewChooserStateDelegate

sealed interface ChangeRecordTypeChooserState : ViewChooserStateDelegate.State {
    object Closed : ChangeRecordTypeChooserState, ViewChooserStateDelegate.State.Closed
    object Color : ChangeRecordTypeChooserState
    object Icon : ChangeRecordTypeChooserState
    object Category : ChangeRecordTypeChooserState
    object GoalTime : ChangeRecordTypeChooserState
    object Additional : ChangeRecordTypeChooserState
    object Type : ChangeRecordTypeChooserState
}
