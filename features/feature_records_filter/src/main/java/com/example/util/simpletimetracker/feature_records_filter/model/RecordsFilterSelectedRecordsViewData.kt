package com.example.util.simpletimetracker.feature_records_filter.model

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class RecordsFilterSelectedRecordsViewData(
    val selectedRecordsCount: String,
    val recordsViewData: List<ViewHolderType>,
)