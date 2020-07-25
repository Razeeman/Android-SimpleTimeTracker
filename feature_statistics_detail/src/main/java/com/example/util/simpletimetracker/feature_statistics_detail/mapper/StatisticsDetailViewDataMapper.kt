package com.example.util.simpletimetracker.feature_statistics_detail.mapper

import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.ChartGrouping
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.ChartLength
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartLengthViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailGroupingViewData
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
        data: List<Long>
    ): StatisticsDetailChartViewData {
        return StatisticsDetailChartViewData(
            data = data
        )
    }

    fun mapToChartGroupingViewData(chartGrouping: ChartGrouping): List<ViewHolderType> {
        return listOf(
            ChartGrouping.DAILY,
            ChartGrouping.WEEKLY,
            ChartGrouping.MONTHLY
        ).map {
            StatisticsDetailGroupingViewData(
                chartGrouping = it,
                name = mapToGroupingName(it),
                color = mapToSelected(it == chartGrouping)
            )
        }
    }

    fun mapToChartLengthViewData(chartLength: ChartLength): List<ViewHolderType> {
        return listOf(
            ChartLength.TEN,
            ChartLength.FIFTY,
            ChartLength.HUNDRED
        ).map {
            StatisticsDetailChartLengthViewData(
                chartLength = it,
                name = mapToLengthName(it),
                color = mapToSelected(it == chartLength)
            )
        }
    }

    private fun mapToGroupingName(chartGrouping: ChartGrouping): String {
        return when (chartGrouping) {
            ChartGrouping.DAILY -> R.string.statistics_detail_chart_daily
            ChartGrouping.WEEKLY -> R.string.statistics_detail_chart_weekly
            ChartGrouping.MONTHLY -> R.string.statistics_detail_chart_monthly
        }.let(resourceRepo::getString)
    }

    private fun mapToLengthName(chartLength: ChartLength): String {
        return when (chartLength) {
            ChartLength.TEN -> R.string.statistics_detail_length_ten
            ChartLength.FIFTY -> R.string.statistics_detail_length_fifty
            ChartLength.HUNDRED -> R.string.statistics_detail_length_hundred
        }.let(resourceRepo::getString)
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