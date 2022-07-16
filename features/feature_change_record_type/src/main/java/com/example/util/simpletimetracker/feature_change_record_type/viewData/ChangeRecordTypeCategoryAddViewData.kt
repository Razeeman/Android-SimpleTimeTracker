package com.example.util.simpletimetracker.feature_change_record_type.viewData

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

object ChangeRecordTypeCategoryAddViewData: ViewHolderType {

    // Only one add item on screen
    override fun getUniqueId(): Long = 1L

    override fun isValidType(other: ViewHolderType): Boolean =
        other is ChangeRecordTypeCategoryAddViewData
}