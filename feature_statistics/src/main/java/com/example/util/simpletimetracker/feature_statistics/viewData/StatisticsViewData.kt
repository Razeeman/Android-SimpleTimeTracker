package com.example.util.simpletimetracker.feature_statistics.viewData

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.example.util.simpletimetracker.core.adapter.ViewHolderType

data class StatisticsViewData(
    val typeId: Long,
    val name: String,
    val duration: String,
    val percent: String,
    @DrawableRes val iconId: Int,
    @ColorInt val color: Int
) : ViewHolderType {

    override fun getViewType(): Int = ViewHolderType.VIEW

    override fun getUniqueId(): Long? = name.hashCode().toLong()
}