package com.example.util.simpletimetracker.feature_statistics_detail.viewData

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class StatisticsDetailViewData<T : ViewHolderType>(
    val data: List<Item<T>>,
    val preview: Preview? = null,
) {

    data class Item<T : ViewHolderType>(
        val item: T,
        val replaceColor: (T.() -> T)? = null,
    )

    data class Preview(
        val previewColor: Int?,
        val comparisonPreviewColor: Int?,
        val mainPreview: StatisticsDetailPreviewViewData?,
    )
}