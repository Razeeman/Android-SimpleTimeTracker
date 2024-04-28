package com.example.util.simpletimetracker.feature_change_record.viewData

sealed interface ChangeRecordPreview {

    object NotAvailable : ChangeRecordPreview

    data class Available(
        val id: Long,
        val before: ChangeRecordSimpleViewData,
        val after: ChangeRecordSimpleViewData,
    ) : ChangeRecordPreview
}