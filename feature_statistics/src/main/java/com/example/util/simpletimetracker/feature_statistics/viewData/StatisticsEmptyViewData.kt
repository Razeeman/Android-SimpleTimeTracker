package com.example.util.simpletimetracker.feature_statistics.viewData

import com.example.util.simpletimetracker.core.adapter.ViewHolderType

data class StatisticsEmptyViewData(
    var message: String
) : ViewHolderType {

    override fun getViewType(): Int = ViewHolderType.FOOTER

    override fun getUniqueId(): Long? = 1L
}