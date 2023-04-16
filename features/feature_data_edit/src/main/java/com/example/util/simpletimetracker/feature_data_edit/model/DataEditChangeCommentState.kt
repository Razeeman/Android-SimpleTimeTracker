package com.example.util.simpletimetracker.feature_data_edit.model

sealed interface DataEditChangeCommentState {

    object Disabled : DataEditChangeCommentState
    data class Enabled(val viewData: String) : DataEditChangeCommentState
}