package com.example.util.simpletimetracker.feature_statistics_detail.viewData

import com.example.util.simpletimetracker.core.adapter.ViewHolderType

data class StatisticsDetailCardViewData(
    val title: String,
    val subtitle: String
) : ViewHolderType {

    override fun getViewType(): Int = ViewHolderType.VIEW
}