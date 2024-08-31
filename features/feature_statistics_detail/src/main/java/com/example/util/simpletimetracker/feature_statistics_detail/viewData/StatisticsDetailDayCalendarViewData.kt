package com.example.util.simpletimetracker.feature_statistics_detail.viewData

import com.example.util.simpletimetracker.core.view.dayCalendar.DayCalendarViewData
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class StatisticsDetailDayCalendarViewData(
    val data: DayCalendarViewData,
) : ViewHolderType {

    override fun getUniqueId(): Long = 1L

    override fun isValidType(other: ViewHolderType): Boolean = other is StatisticsDetailDayCalendarViewData
}