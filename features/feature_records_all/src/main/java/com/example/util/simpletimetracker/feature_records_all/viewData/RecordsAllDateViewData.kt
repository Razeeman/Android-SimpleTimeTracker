package com.example.util.simpletimetracker.feature_records_all.viewData

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class RecordsAllDateViewData(
    var message: String
) : ViewHolderType {

    override fun getUniqueId(): Long = message.hashCode().toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is RecordsAllDateViewData
}