package com.example.util.simpletimetracker.feature_statistics.viewData

import com.example.util.simpletimetracker.core.adapter.ViewHolderType

data class StatisticsInfoViewData(
    val name: String,
    val text: String
) : ViewHolderType {

    override fun getViewType(): Int = ViewHolderType.INFO

    override fun getUniqueId(): Long? = name.hashCode().toLong()
}