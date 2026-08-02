package com.example.util.simpletimetracker.feature_change_record.api.viewData

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class ChangeRecordTagsViewData(
    val selectedCount: Int,
    val viewData: List<ViewHolderType>,
)