package com.example.util.simpletimetracker.feature_statistics_detail.viewData

data class StatisticsDetailPreviewCompositeViewData(
    val data: List<StatisticsDetailViewData<*>>,
    val preview: Preview?,
) {

    data class Preview(
        val previewColor: Int?,
        val comparisonPreviewColor: Int?,
        val mainPreview: StatisticsDetailPreviewViewData?,
    )
}