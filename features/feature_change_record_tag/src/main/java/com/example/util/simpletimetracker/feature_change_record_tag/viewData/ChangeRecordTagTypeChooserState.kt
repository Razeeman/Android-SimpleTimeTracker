package com.example.util.simpletimetracker.feature_change_record_tag.viewData

data class ChangeRecordTagTypeChooserState(
    val current: State,
    val previous: State,
) {

    // TODO TAGS object Icon : State
    sealed interface State {
        object Closed : State
        object Color : State
        object Type : State
    }
}