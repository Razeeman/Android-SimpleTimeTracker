package com.example.util.simpletimetracker.core.viewData

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.example.util.simpletimetracker.core.adapter.ViewHolderType

data class RecordTypeViewData(
    val id: Long,
    val name: String,
    @DrawableRes val iconId: Int,
    @ColorInt val iconColor: Int,
    @ColorInt val color: Int,
    val width: Int? = null,
    val height: Int? = null,
    val asRow: Boolean = false
) : ViewHolderType {

    override fun getUniqueId(): Long = id

    override fun isValidType(other: ViewHolderType): Boolean = other is RecordTypeViewData
}