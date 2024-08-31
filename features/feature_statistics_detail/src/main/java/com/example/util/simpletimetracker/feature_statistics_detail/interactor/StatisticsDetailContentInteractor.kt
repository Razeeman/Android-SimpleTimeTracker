package com.example.util.simpletimetracker.feature_statistics_detail.interactor

import android.graphics.Color
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.OneShotValue
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailBarChartViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailBlock
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailButtonsRowViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailCardDoubleViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailCardViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailHintViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailNextActivitiesViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailPreviewsViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailSeriesCalendarViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailSeriesChartViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartCompositeViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreviewCompositeViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreviewViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailStatsViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailStreaksViewData
import javax.inject.Inject

class StatisticsDetailContentInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
) {

    fun getContent(
        previewViewData: StatisticsDetailPreviewCompositeViewData?,
        chartViewData: StatisticsDetailChartCompositeViewData?,
        dailyCalendarViewData: List<ViewHolderType>?,
        statsViewData: StatisticsDetailStatsViewData?,
        streaksViewData: StatisticsDetailStreaksViewData?,
        streaksGoalViewData: List<ViewHolderType>?,
        streaksTypeViewData: List<ViewHolderType>?,
        splitChartViewData: StatisticsDetailChartViewData?,
        comparisonSplitChartViewData: StatisticsDetailChartViewData?,
        splitChartGroupingViewData: List<ViewHolderType>?,
        durationSplitChartViewData: StatisticsDetailChartViewData?,
        comparisonDurationSplitChartViewData: StatisticsDetailChartViewData?,
        nextActivitiesViewData: List<ViewHolderType>?,
    ): List<ViewHolderType> {
        val result = mutableListOf<ViewHolderType>()

        fun getPreviewColor(): Int {
            return previewViewData
                ?.data?.color
                ?: Color.BLACK
        }

        fun getPreviewColorComparison(): Int {
            return previewViewData
                ?.comparisonData
                ?.filterIsInstance<StatisticsDetailPreviewViewData>()
                ?.firstOrNull()
                ?.color
                ?: Color.BLACK
        }

        previewViewData?.let { viewData ->
            val rest: List<ViewHolderType> = viewData.additionalData + viewData.comparisonData
            if (rest.isEmpty()) return@let
            result += StatisticsDetailPreviewsViewData(
                block = StatisticsDetailBlock.PreviewItems,
                data = rest,
            )
        }

        chartViewData?.let { viewData ->
            val comparisonChartIsVisible = viewData.showComparison &&
                viewData.chartData.visible &&
                viewData.compareChartData.visible

            if (viewData.chartData.visible) {
                result += StatisticsDetailBarChartViewData(
                    block = StatisticsDetailBlock.ChartData,
                    color = getPreviewColor(),
                    marginTopDp = 16,
                    data = viewData.chartData,
                )
            }

            if (viewData.compareChartData.visible && comparisonChartIsVisible) {
                result += StatisticsDetailBarChartViewData(
                    block = StatisticsDetailBlock.ChartDataComparison,
                    color = getPreviewColorComparison(),
                    marginTopDp = 16,
                    data = viewData.compareChartData,
                )
            }

            if (viewData.chartGroupingVisible) {
                result += StatisticsDetailButtonsRowViewData(
                    block = StatisticsDetailBlock.ChartGrouping,
                    marginTopDp = 4,
                    data = viewData.chartGroupingViewData,
                )
            }

            if (viewData.chartLengthVisible) {
                result += StatisticsDetailButtonsRowViewData(
                    block = StatisticsDetailBlock.ChartLength,
                    marginTopDp = -10,
                    data = viewData.chartLengthViewData,
                )
            }

            val rangeAveragesData = viewData.rangeAverages
            if (rangeAveragesData.isNotEmpty()) {
                result += StatisticsDetailCardViewData(
                    block = StatisticsDetailBlock.RangeAverages,
                    title = viewData.rangeAveragesTitle,
                    marginTopDp = 0,
                    data = rangeAveragesData,
                )
            }
        }

        dailyCalendarViewData?.let {
            result += it
        }

        statsViewData?.let { viewData ->
            result += StatisticsDetailCardDoubleViewData(
                block = StatisticsDetailBlock.Total,
                first = viewData.totalDuration,
                second = viewData.timesTracked,
            )
            result += StatisticsDetailCardViewData(
                block = StatisticsDetailBlock.Average,
                title = resourceRepo.getString(R.string.statistics_detail_record_length),
                marginTopDp = 4,
                data = viewData.averageRecord,
            )
            result += StatisticsDetailCardViewData(
                block = StatisticsDetailBlock.Dates,
                title = resourceRepo.getString(R.string.statistics_detail_record_time),
                marginTopDp = 4,
                data = viewData.datesTracked,
            )
        }

        streaksViewData?.let { viewData ->
            result += StatisticsDetailCardViewData(
                block = StatisticsDetailBlock.Series,
                title = resourceRepo.getString(R.string.statistics_detail_streaks),
                marginTopDp = 4,
                data = viewData.streaks,
            )
        }

        streaksGoalViewData?.let { viewData ->
            if (viewData.isNotEmpty()) {
                result += StatisticsDetailButtonsRowViewData(
                    block = StatisticsDetailBlock.SeriesGoal,
                    marginTopDp = 0,
                    data = viewData,
                )
            }
        }

        streaksViewData?.let { viewData ->
            if (viewData.showData) {
                result += StatisticsDetailSeriesChartViewData(
                    block = StatisticsDetailBlock.SeriesChart,
                    color = getPreviewColor(),
                    data = viewData.data,
                    animate = OneShotValue(true),
                )
            }
            if (viewData.showComparison) {
                result += StatisticsDetailSeriesChartViewData(
                    block = StatisticsDetailBlock.SeriesChartComparison,
                    color = getPreviewColorComparison(),
                    data = viewData.compareData,
                    animate = OneShotValue(true),
                )
            }
            if (viewData.showData) {
                result += StatisticsDetailButtonsRowViewData(
                    block = StatisticsDetailBlock.SeriesType,
                    marginTopDp = 4,
                    data = streaksTypeViewData.orEmpty(),
                )
            }
            if (viewData.showCalendar) {
                result += StatisticsDetailSeriesCalendarViewData(
                    block = StatisticsDetailBlock.SeriesCalendar,
                    color = getPreviewColor(),
                    data = viewData.calendarData,
                    rowsCount = viewData.calendarRowsCount,
                )
            }
            if (viewData.showComparisonCalendar) {
                result += StatisticsDetailSeriesCalendarViewData(
                    block = StatisticsDetailBlock.SeriesCalendarComparison,
                    color = getPreviewColorComparison(),
                    data = viewData.compareCalendarData,
                    rowsCount = viewData.calendarRowsCount,
                )
            }
            if (viewData.completion.isNotEmpty()) {
                result += StatisticsDetailCardViewData(
                    block = StatisticsDetailBlock.SeriesCompletion,
                    title = resourceRepo.getString(R.string.statistics_detail_streaks_completion),
                    marginTopDp = 8,
                    data = viewData.completion,
                )
            }
        }

        splitChartViewData?.let { viewData ->
            if (viewData.visible) {
                result += StatisticsDetailHintViewData(
                    block = StatisticsDetailBlock.SplitHint,
                    text = resourceRepo.getString(R.string.statistics_detail_day_split_hint),
                )
                result += StatisticsDetailBarChartViewData(
                    block = StatisticsDetailBlock.SplitChart,
                    color = getPreviewColor(),
                    marginTopDp = 0,
                    data = viewData,
                )
            }
        }

        comparisonSplitChartViewData?.let { viewData ->
            if (viewData.visible) {
                result += StatisticsDetailBarChartViewData(
                    block = StatisticsDetailBlock.SplitChartComparison,
                    color = getPreviewColorComparison(),
                    marginTopDp = 0,
                    data = viewData,
                )
            }
        }

        splitChartGroupingViewData?.let { viewData ->
            if (viewData.isNotEmpty()) {
                result += StatisticsDetailButtonsRowViewData(
                    block = StatisticsDetailBlock.SplitChartGrouping,
                    marginTopDp = 4,
                    data = viewData,
                )
            }
        }

        durationSplitChartViewData?.let { viewData ->
            if (viewData.visible) {
                result += StatisticsDetailHintViewData(
                    block = StatisticsDetailBlock.DurationSplitHint,
                    text = resourceRepo.getString(R.string.statistics_detail_duration_split_hint),
                )
                result += StatisticsDetailBarChartViewData(
                    block = StatisticsDetailBlock.DurationSplitChart,
                    color = getPreviewColor(),
                    marginTopDp = 0,
                    data = viewData,
                )
            }
        }

        comparisonDurationSplitChartViewData?.let { viewData ->
            if (viewData.visible) {
                result += StatisticsDetailBarChartViewData(
                    block = StatisticsDetailBlock.DurationSplitChartComparison,
                    color = getPreviewColorComparison(),
                    marginTopDp = 0,
                    data = viewData,
                )
            }
        }

        nextActivitiesViewData?.let { viewData ->
            if (viewData.isNotEmpty()) {
                result += StatisticsDetailNextActivitiesViewData(
                    block = StatisticsDetailBlock.NextActivities,
                    data = viewData,
                )
            }
        }

        statsViewData?.let { viewData ->
            result += viewData.splitData
        }

        return result
    }
}