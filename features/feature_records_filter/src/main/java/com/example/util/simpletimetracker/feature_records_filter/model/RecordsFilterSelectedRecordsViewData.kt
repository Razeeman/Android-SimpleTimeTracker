package com.example.util.simpletimetracker.feature_records_filter.model

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData

data class RecordsFilterSelectedRecordsViewData(
    val isLoading: Boolean,
    val selectedRecordsCount: String,
    val showListButtonIsVisible: Boolean,
    val recordsViewData: List<ViewHolderType>,
) {

    companion object {
        val Loading = RecordsFilterSelectedRecordsViewData(
            isLoading = true,
            selectedRecordsCount = "",
            showListButtonIsVisible = false,
            recordsViewData = listOf(LoaderViewData()),
        )
    }
}