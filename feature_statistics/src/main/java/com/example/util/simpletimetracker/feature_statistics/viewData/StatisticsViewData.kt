package com.example.util.simpletimetracker.feature_statistics.viewData

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.example.util.simpletimetracker.core.adapter.ViewHolderType

sealed class StatisticsViewData : ViewHolderType {
    abstract val id: Long
    abstract val name: String
    abstract val duration: String
    abstract val percent: String
    abstract val color: Int

    override fun getViewType(): Int = ViewHolderType.VIEW

    override fun getUniqueId(): Long? = id

    data class StatisticsActivityViewData(
        override val id: Long,
        override val name: String,
        override val duration: String,
        override val percent: String,
        @ColorInt override val color: Int,
        @DrawableRes val iconId: Int
    ) : StatisticsViewData()

    data class StatisticsCategoryViewData(
        override val id: Long,
        override val name: String,
        override val duration: String,
        override val percent: String,
        @ColorInt override val color: Int
    ) : StatisticsViewData()
}