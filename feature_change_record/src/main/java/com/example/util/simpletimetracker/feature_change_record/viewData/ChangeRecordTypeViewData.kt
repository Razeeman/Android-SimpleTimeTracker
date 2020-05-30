package com.example.util.simpletimetracker.feature_change_record.viewData

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.example.util.simpletimetracker.core.adapter.ViewHolderType

data class ChangeRecordTypeViewData(
    val id: Long,
    val name: String,
    @DrawableRes val icon: Int,
    @ColorInt val color: Int
) : ViewHolderType {

    override fun getViewType(): Int = ViewHolderType.VIEW

    override fun getUniqueId(): Long? = id
}