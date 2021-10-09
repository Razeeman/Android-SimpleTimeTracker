package com.example.util.simpletimetracker.feature_base_adapter.statisticsTag

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon

data class StatisticsTagViewData(
    val id: Long,
    val name: String,
    val duration: String,
    val percent: String,
    @ColorInt val color: Int,
    val icon: RecordTypeIcon,
) : ViewHolderType {

    override fun getUniqueId(): Long = id

    override fun isValidType(other: ViewHolderType): Boolean = other is StatisticsTagViewData
}