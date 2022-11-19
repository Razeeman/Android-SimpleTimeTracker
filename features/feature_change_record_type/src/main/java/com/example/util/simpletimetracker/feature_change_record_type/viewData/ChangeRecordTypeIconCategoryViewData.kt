package com.example.util.simpletimetracker.feature_change_record_type.viewData

import androidx.annotation.DrawableRes
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class ChangeRecordTypeIconCategoryViewData(
    val type: ChangeRecordTypeIconTypeViewData,
    @DrawableRes val categoryIcon: Int,
    val selected: Boolean,
) : ViewHolderType {

    override fun getUniqueId(): Long = type.id

    override fun isValidType(other: ViewHolderType): Boolean = other is ChangeRecordTypeIconCategoryViewData
}