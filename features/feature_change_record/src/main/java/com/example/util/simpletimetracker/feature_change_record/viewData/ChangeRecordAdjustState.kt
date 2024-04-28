package com.example.util.simpletimetracker.feature_change_record.viewData

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class ChangeRecordAdjustState(
    val currentData: ChangeRecordPreview,
    val changesPreview: List<ViewHolderType>,
)