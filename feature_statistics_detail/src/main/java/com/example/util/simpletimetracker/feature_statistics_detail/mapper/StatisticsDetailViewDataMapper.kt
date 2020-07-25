package com.example.util.simpletimetracker.feature_statistics_detail.mapper

import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.ChartGrouping
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailViewData
import javax.inject.Inject

class StatisticsDetailViewDataMapper @Inject constructor(
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val timeMapper: TimeMapper,
    private val resourceRepo: ResourceRepo
) {

    fun map(
        records: List<Record>,
        recordType: RecordType?
    ): StatisticsDetailViewData {
        val totalDuration = records.let(::mapToDuration)
        val timesTracked = records.size.toLong()

        return StatisticsDetailViewData(
            name = recordType?.name.orEmpty(),
            iconId = recordType?.icon
                ?.let(iconMapper::mapToDrawableResId)
                ?: R.drawable.unknown,
            color = (recordType?.color
                ?.let(colorMapper::mapToColorResId)
                ?: R.color.untracked_time_color)
                .let(resourceRepo::getColor),
            totalDuration = totalDuration
                .let(timeMapper::formatInterval),
            timesTracked = timesTracked.toString()
        )
    }

    fun mapToUntracked(): StatisticsDetailViewData {
        return StatisticsDetailViewData(
            name = resourceRepo.getString(R.string.untracked_time_name),
            iconId = R.drawable.unknown,
            color = R.color.untracked_time_color.let(resourceRepo::getColor),
            totalDuration = "",
            timesTracked = "0"
        )
    }

    fun mapToChartViewData(
        data: List<Long>,
        grouping: ChartGrouping
    ): StatisticsDetailChartViewData {
        return StatisticsDetailChartViewData(
            data = data,
            dailyButtonColor = mapToSelected(grouping == ChartGrouping.DAILY),
            weeklyButtonColor = mapToSelected(grouping == ChartGrouping.WEEKLY),
            monthlyButtonColor = mapToSelected(grouping == ChartGrouping.MONTHLY)
        )
    }

    private fun mapToSelected(isSelected: Boolean): Int {
        return (if (isSelected) R.color.colorPrimary else R.color.blue_grey_300)
            .let(resourceRepo::getColor)
    }

    private fun mapToDuration(records: List<Record>): Long {
        return records
            .map { it.timeEnded - it.timeStarted }
            .sum()
    }
}