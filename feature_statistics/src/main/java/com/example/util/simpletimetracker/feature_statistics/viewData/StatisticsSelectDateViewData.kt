package com.example.util.simpletimetracker.feature_statistics.viewData

import com.example.util.simpletimetracker.core.adapter.ViewHolderType

data class StatisticsSelectDateViewData(
    val name: String
) : ViewHolderType {

    override fun getViewType(): Int = ViewHolderType.HEADER

    override fun getUniqueId(): Long? = name.hashCode().toLong()
}