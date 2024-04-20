package com.example.util.simpletimetracker.feature_change_record_tag.viewData

data class ChangeRecordTagTypeChooserState(
    val current: State,
    val previous: State,
) {

    sealed interface State {
        object Closed : State
        object Color : State
        object Icon : State
        object Type : State
        object DefaultType : State
    }
}