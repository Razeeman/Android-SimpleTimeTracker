package com.example.util.simpletimetracker.feature_statistics.viewData

import com.example.util.simpletimetracker.core.adapter.ViewHolderType

data class StatisticsRangeViewData(
    val rangeLength: RangeLength,
    val name: String
) : ViewHolderType {

    override fun getViewType(): Int = ViewHolderType.VIEW

    override fun getUniqueId(): Long? = name.hashCode().toLong()
}