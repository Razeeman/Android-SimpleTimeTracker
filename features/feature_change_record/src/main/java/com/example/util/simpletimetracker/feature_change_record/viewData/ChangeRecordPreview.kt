package com.example.util.simpletimetracker.feature_change_record.viewData

import com.example.util.simpletimetracker.feature_change_record.api.viewData.ChangeRecordSimpleViewData

data class ChangeRecordPreview(
    val id: Long,
    val before: ChangeRecordSimpleViewData,
    val after: ChangeRecordSimpleViewData,
)
