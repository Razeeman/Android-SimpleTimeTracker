package com.example.util.simpletimetracker.core.viewData

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.example.util.simpletimetracker.core.adapter.ViewHolderType

data class RecordTypeViewData(
    val id: Long,
    val name: String,
    @DrawableRes val iconId: Int,
    @ColorInt val color: Int
) : ViewHolderType {

    override fun getViewType(): Int = ViewHolderType.RECORD_TYPE

    override fun getUniqueId(): Long? = id
}