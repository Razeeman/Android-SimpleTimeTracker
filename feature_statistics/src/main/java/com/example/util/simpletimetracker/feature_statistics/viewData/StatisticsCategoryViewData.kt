package com.example.util.simpletimetracker.feature_statistics.viewData

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.core.adapter.ViewHolderType

data class StatisticsCategoryViewData(
    val categoryId: Long,
    val name: String,
    val duration: String,
    val percent: String,
    @ColorInt val color: Int
) : ViewHolderType {

    override fun getViewType(): Int = ViewHolderType.VIEW2

    override fun getUniqueId(): Long? = categoryId
}