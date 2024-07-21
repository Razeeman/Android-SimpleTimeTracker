package com.example.util.simpletimetracker.feature_change_record.viewData

data class ChangeRecordPreview(
    val id: Long,
    val before: ChangeRecordSimpleViewData,
    val after: ChangeRecordSimpleViewData,
)
