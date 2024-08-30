package com.example.util.simpletimetracker.feature_statistics.viewData

import com.example.util.simpletimetracker.core.view.dayCalendar.DayCalendarViewData
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class StatisticsDayCalendarViewData(
    val data: DayCalendarViewData,
) : ViewHolderType {

    override fun getUniqueId(): Long = 1L

    override fun isValidType(other: ViewHolderType): Boolean = other is StatisticsDayCalendarViewData
}