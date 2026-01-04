package com.example.util.simpletimetracker.feature_change_record_tag.viewData

import com.example.util.simpletimetracker.core.view.ViewChooserStateDelegate

sealed interface ChangeRecordTagChooserState : ViewChooserStateDelegate.State {
    data object Closed : ChangeRecordTagChooserState, ViewChooserStateDelegate.State.Closed
    data object Color : ChangeRecordTagChooserState
    data object Icon : ChangeRecordTagChooserState
    data object Type : ChangeRecordTagChooserState
    data object DefaultType : ChangeRecordTagChooserState
    data object ValueType : ChangeRecordTagChooserState
    data object GoalTime : ChangeRecordTagChooserState
}