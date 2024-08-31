package com.example.util.simpletimetracker.feature_statistics_detail.mapper

import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.view.dayCalendar.DayCalendarViewData
import com.example.util.simpletimetracker.domain.model.RecordBase
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailDailyCalendarViewDataInteractor.RecordHolder
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailDayCalendarViewData
import java.util.Calendar
import javax.inject.Inject

class StatisticsDetailDailyCalendarViewDataMapper @Inject constructor(
    private val timeMapper: TimeMapper,
) {

    fun mapToEmpty(): StatisticsDetailDayCalendarViewData {
        return StatisticsDetailDayCalendarViewData(
            data = DayCalendarViewData(emptyList()),
        )
    }

    fun mapToCalendarPoint(
        holder: RecordHolder,
        calendar: Calendar,
        startOfDayShift: Long,
    ): DayCalendarViewData.Point {
        val start = timeMapper.mapFromStartOfDay(
            // Normalize to set start of day correctly.
            timeStamp = holder.timeStartedTimestamp - startOfDayShift,
            calendar = calendar,
        ) + startOfDayShift
        val duration = holder.timeEndedTimestamp - holder.timeStartedTimestamp
        val end = start + duration

        return DayCalendarViewData.Point(
            start = start - startOfDayShift,
            end = end - startOfDayShift,
            data = DayCalendarViewData.Point.Data(
                color = holder.color,
            ),
        )
    }

    fun mapRecordHolder(
        recordBase: RecordBase,
        color: Int,
    ): RecordHolder {
        return RecordHolder(
            recordBase.timeStarted,
            recordBase.timeEnded,
            color,
        )
    }
}