package com.example.util.simpletimetracker.feature_statistics_detail.viewData

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class StatisticsDetailPreviewCompositeViewData<T : ViewHolderType>(
    val data: List<StatisticsDetailViewData.Item<T>>,
    val preview: Preview?,
) {

    data class Preview(
        val previewColor: Int?,
        val comparisonPreviewColor: Int?,
        val mainPreview: StatisticsDetailPreviewViewData?,
    )
}