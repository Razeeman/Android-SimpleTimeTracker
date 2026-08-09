package com.example.util.simpletimetracker.feature_statistics_detail.interactor

import android.graphics.Color
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.base.OneShotValue
import com.example.util.simpletimetracker.domain.extension.plusAssign
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.buttonsRow.ButtonsRowItemViewData
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailBarChartViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailBlock
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailCardDoubleViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailCardViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailHintViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailNextActivitiesViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailSeriesCalendarViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailSeriesChartViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartCompositeViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailDataDistributionViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailGoalsCompositeViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreviewCompositeViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailStatsViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailStreaksViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailTagValuesCompositeViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailViewData
import javax.inject.Inject

class StatisticsDetailContentInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
) {

    fun getContent(
        previewViewData: StatisticsDetailPreviewCompositeViewData.Preview?,
        data: List<StatisticsDetailViewData.Item<out ViewHolderType>>,
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
        goalsViewData: StatisticsDetailGoalsCompositeViewData?,
        dataDistributionViewData: StatisticsDetailDataDistributionViewData?,
        tagValueViewData: StatisticsDetailTagValuesCompositeViewData?,
    ): List<ViewHolderType> {
        fun getPreviewColor(): Int = previewViewData?.previewColor ?: Color.BLACK
        fun getPreviewColorComparison(): Int = previewViewData?.comparisonPreviewColor ?: Color.BLACK

        val result = mutableListOf<ViewHolderType>()

        result += data.map { it.itemProducer?.invoke(previewViewData) ?: it.item }

        result += dailyCalendarViewData

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
                result += ButtonsRowItemViewData(
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
                result += ButtonsRowItemViewData(
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
            result += StatisticsDetailHintViewData(
                block = StatisticsDetailBlock.SplitHint,
                text = resourceRepo.getString(R.string.statistics_detail_day_split_hint),
            )
            result += StatisticsDetailBarChartViewData(
                block = StatisticsDetailBlock.SplitChart,
                singleColor = getPreviewColor(),
                marginTopDp = 0,
                data = viewData,
            )
        }

        comparisonSplitChartViewData?.let { viewData ->
            result += StatisticsDetailBarChartViewData(
                block = StatisticsDetailBlock.SplitChartComparison,
                singleColor = getPreviewColorComparison(),
                marginTopDp = 0,
                data = viewData,
            )
        }

        splitChartGroupingViewData?.let { viewData ->
            if (viewData.isNotEmpty()) {
                result += ButtonsRowItemViewData(
                    block = StatisticsDetailBlock.SplitChartGrouping,
                    marginTopDp = 4,
                    data = viewData,
                )
            }
        }

        durationSplitChartViewData?.let { viewData ->
            result += StatisticsDetailHintViewData(
                block = StatisticsDetailBlock.DurationSplitHint,
                text = resourceRepo.getString(R.string.statistics_detail_duration_split_hint),
            )
            result += StatisticsDetailBarChartViewData(
                block = StatisticsDetailBlock.DurationSplitChart,
                singleColor = getPreviewColor(),
                marginTopDp = 0,
                data = viewData,
            )
        }

        comparisonDurationSplitChartViewData?.let { viewData ->
            result += StatisticsDetailBarChartViewData(
                block = StatisticsDetailBlock.DurationSplitChartComparison,
                singleColor = getPreviewColorComparison(),
                marginTopDp = 0,
                data = viewData,
            )
        }

        nextActivitiesViewData?.let { viewData ->
            if (viewData.isNotEmpty()) {
                result += StatisticsDetailNextActivitiesViewData(
                    block = StatisticsDetailBlock.NextActivities,
                    data = viewData,
                )
            }
        }

        result += goalsViewData?.viewData.orEmpty()

        result += tagValueViewData?.viewData.orEmpty().map {
            if (it is StatisticsDetailBarChartViewData) {
                it.copy(singleColor = getPreviewColor())
            } else {
                it
            }
        }

        result += dataDistributionViewData?.splitData.orEmpty()

        return result
    }
}