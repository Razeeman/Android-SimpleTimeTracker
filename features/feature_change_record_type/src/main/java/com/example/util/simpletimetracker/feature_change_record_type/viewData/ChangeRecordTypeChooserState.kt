package com.example.util.simpletimetracker.feature_change_record_type.viewData

sealed interface ChangeRecordTypeChooserState {
    object Closed : ChangeRecordTypeChooserState
    object Color : ChangeRecordTypeChooserState
    object Icon : ChangeRecordTypeChooserState
    object Category : ChangeRecordTypeChooserState
}
