package com.example.util.simpletimetracker.feature_records_all.viewData

import com.example.util.simpletimetracker.core.adapter.ViewHolderType

data class RecordsAllDateViewData(
    var message: String
) : ViewHolderType {

    override fun getViewType(): Int = ViewHolderType.DIVIDER

    override fun getUniqueId(): Long? = message.hashCode().toLong()
}