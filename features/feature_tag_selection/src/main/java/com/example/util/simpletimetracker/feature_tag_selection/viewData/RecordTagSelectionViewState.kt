package com.example.util.simpletimetracker.feature_tag_selection.viewData

data class RecordTagSelectionViewState(
    val fields: List<Field>,
) {

    sealed interface Field {
        object Tags : Field
        object Comment : Field
    }
}