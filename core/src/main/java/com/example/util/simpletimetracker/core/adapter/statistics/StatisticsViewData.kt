package com.example.util.simpletimetracker.core.adapter.statistics

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon

sealed class StatisticsViewData : ViewHolderType {
    abstract val id: Long
    abstract val name: String
    abstract val duration: String
    abstract val percent: String
    abstract val color: Int

    override fun getUniqueId(): Long = id

    override fun isValidType(other: ViewHolderType): Boolean = other is StatisticsViewData

    data class Activity(
        override val id: Long,
        override val name: String,
        override val duration: String,
        override val percent: String,
        @ColorInt override val color: Int,
        val icon: RecordTypeIcon
    ) : StatisticsViewData()

    data class Category(
        override val id: Long,
        override val name: String,
        override val duration: String,
        override val percent: String,
        @ColorInt override val color: Int
    ) : StatisticsViewData()
}