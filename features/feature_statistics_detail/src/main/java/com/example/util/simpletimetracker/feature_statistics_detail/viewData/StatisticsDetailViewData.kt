package com.example.util.simpletimetracker.feature_statistics_detail.viewData

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class StatisticsDetailViewData<T : ViewHolderType>(
    val data: List<Item<T>>,
) {

    data class Item<T : ViewHolderType>(
        val item: T,
        val itemProducer: ((StatisticsDetailPreviewCompositeViewData.Preview?) -> T)? = null,
    )
}