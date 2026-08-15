package com.example.util.simpletimetracker.feature_statistics_detail.interactor

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreviewCompositeViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailViewData
import javax.inject.Inject

class StatisticsDetailContentInteractor @Inject constructor() {

    fun getContent(
        previewViewData: StatisticsDetailPreviewCompositeViewData.Preview?,
        data: List<StatisticsDetailViewData<*>>,
    ): List<ViewHolderType> {
        return data.map { it.itemProducer?.invoke(previewViewData) ?: it.item }
    }
}