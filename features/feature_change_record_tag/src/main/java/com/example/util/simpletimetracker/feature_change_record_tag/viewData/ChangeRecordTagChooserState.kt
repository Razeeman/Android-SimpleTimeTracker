package com.example.util.simpletimetracker.feature_change_record_tag.viewData

import com.example.util.simpletimetracker.core.view.ViewChooserStateDelegate

sealed interface ChangeRecordTagChooserState : ViewChooserStateDelegate.State {
    object Closed : ChangeRecordTagChooserState, ViewChooserStateDelegate.State.Closed
    object Color : ChangeRecordTagChooserState
    object Icon : ChangeRecordTagChooserState
    object Type : ChangeRecordTagChooserState
    object DefaultType : ChangeRecordTagChooserState
}