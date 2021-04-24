package com.example.util.simpletimetracker.feature_change_record_type.viewData

import com.example.util.simpletimetracker.core.adapter.ViewHolderType

data class ChangeRecordTypeIconCategoryViewData(
    val type: ChangeRecordTypeIconTypeViewData,
    val categoryIcon: Int
) : ViewHolderType {

    override fun getUniqueId(): Long = type.id

    override fun isValidType(other: ViewHolderType): Boolean = other is ChangeRecordTypeIconCategoryViewData
}