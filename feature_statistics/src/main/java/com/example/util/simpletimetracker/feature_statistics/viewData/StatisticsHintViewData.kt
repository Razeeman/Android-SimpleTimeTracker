package com.example.util.simpletimetracker.feature_statistics.viewData

import com.example.util.simpletimetracker.core.adapter.ViewHolderType

data class StatisticsHintViewData(
    val text: String
) : ViewHolderType {

    override fun getViewType(): Int = ViewHolderType.HINT

    override fun getUniqueId(): Long? = text.hashCode().toLong()
}