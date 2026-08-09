package com.example.util.simpletimetracker.feature_statistics_detail.interactor

import android.graphics.Color
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.base.OneShotValue
import com.example.util.simpletimetracker.domain.extension.plusAssign
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.buttonsRow.ButtonsRowItemViewData
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailBlock
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailCardViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailSeriesCalendarViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailSeriesChartViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreviewCompositeViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailStreaksViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailViewData
import javax.inject.Inject

class StatisticsDetailContentInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
) {

    fun getContent(
        previewViewData: StatisticsDetailPreviewCompositeViewData.Preview?,
        data: List<StatisticsDetailViewData<*>>,
        streaksViewData: StatisticsDetailStreaksViewData?,
        streaksGoalViewData: List<ViewHolderType>?,
        streaksTypeViewData: List<ViewHolderType>?,
        splitChartViewData: List<StatisticsDetailViewData<*>>?,
        comparisonSplitChartViewData: List<StatisticsDetailViewData<*>>?,
        splitChartGroupingViewData: List<ViewHolderType>?,
        durationSplitChartViewData: List<StatisticsDetailViewData<*>>?,
        comparisonDurationSplitChartViewData: List<StatisticsDetailViewData<*>>?,
        nextActivitiesViewData: List<ViewHolderType>?,
        goalsViewData: List<ViewHolderType>?,
        dataDistributionViewData: List<ViewHolderType>?,
        tagValueViewData: List<StatisticsDetailViewData<*>>?,
    ): List<ViewHolderType> {
        fun List<StatisticsDetailViewData<*>>.mapToItems(): List<ViewHolderType> =
            this.map { it.itemProducer?.invoke(previewViewData) ?: it.item }

        fun getPreviewColor(): Int = previewViewData?.previewColor ?: Color.BLACK
        fun getPreviewColorComparison(): Int = previewViewData?.comparisonPreviewColor ?: Color.BLACK

        val result = mutableListOf<ViewHolderType>()

        result += data.mapToItems()

        result += streaksViewData?.streaks
        result += streaksGoalViewData

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

        result += splitChartViewData?.mapToItems()
        result += comparisonSplitChartViewData?.mapToItems()
        result += splitChartGroupingViewData
        result += durationSplitChartViewData?.mapToItems()
        result += comparisonDurationSplitChartViewData?.mapToItems()
        result += nextActivitiesViewData
        result += goalsViewData
        result += tagValueViewData?.mapToItems()
        result += dataDistributionViewData

        return result
    }
}