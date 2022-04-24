package com.example.util.simpletimetracker.feature_statistics_detail.viewData

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class StatisticsDetailPreviewCompositeViewData(
    val data: StatisticsDetailPreviewViewData?,
    val additionalData: List<ViewHolderType>,
    val comparisonData: List<ViewHolderType>,
)