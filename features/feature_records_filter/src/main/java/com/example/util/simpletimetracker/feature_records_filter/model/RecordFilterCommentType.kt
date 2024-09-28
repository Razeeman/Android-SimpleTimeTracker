package com.example.util.simpletimetracker.feature_records_filter.model

sealed interface RecordFilterCommentType {
    object NoComment : RecordFilterCommentType
    object AnyComment : RecordFilterCommentType
}