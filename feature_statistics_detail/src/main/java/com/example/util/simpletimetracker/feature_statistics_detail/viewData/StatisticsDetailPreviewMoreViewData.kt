package com.example.util.simpletimetracker.feature_statistics_detail.viewData

import com.example.util.simpletimetracker.core.adapter.ViewHolderType

object StatisticsDetailPreviewMoreViewData : ViewHolderType {

    // Only one item in recycler
    override fun getUniqueId(): Long = 1L

    override fun isValidType(other: ViewHolderType): Boolean =
        other is StatisticsDetailPreviewMoreViewData
}