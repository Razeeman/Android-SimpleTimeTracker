package com.example.util.simpletimetracker.feature_statistics_detail.mapper

import com.example.util.simpletimetracker.core.extension.removeTrailingZeroes
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.base.DurationFormat
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.buttonsRow.ButtonsRowItemViewData
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailBarChartViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailBlock
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailButtonViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailCardViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailHintViewData
import com.example.util.simpletimetracker.feature_statistics_detail.conts.TAG_VALUE_PRECISION
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartBarDataDuration
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartGrouping
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartLength
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartMode
import com.example.util.simpletimetracker.domain.statistics.model.ChartValueMode
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailCardInternalViewData
import javax.inject.Inject

class StatisticsDetailTagValuesViewDataMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val statisticsDetailViewDataMapper: StatisticsDetailViewDataMapper,
) {

    fun mapTagValueChartViewData(
        data: List<ChartBarDataDuration>,
        prevData: List<ChartBarDataDuration>,
        rangeLength: RangeLength,
        availableChartGroupings: List<ChartGrouping>,
        appliedChartGrouping: ChartGrouping,
        availableChartLengths: List<ChartLength>,
        appliedChartLength: ChartLength,
        chartMode: ChartMode.TAG_VALUE,
        chartValueMode: ChartValueMode,
        yAxisZoomed: Boolean,
        valueSuffix: String,
        durationFormat: DurationFormat,
        showSeconds: Boolean,
        isDarkTheme: Boolean,
    ): List<ViewHolderType> {
        val items = mutableListOf<ViewHolderType>()

        val chartData = statisticsDetailViewDataMapper.mapChartData(
            data = data,
            goal = 0, // Don't show goal.
            rangeLength = rangeLength,
            chartMode = chartMode,
            yAxisZoomed = yAxisZoomed,
            showSelectedBarOnStart = true,
            useSingleColor = true,
            drawRoundCaps = true,
        )
        val (title, rangeAverages) = statisticsDetailViewDataMapper.getRangeAverages(
            data = data,
            prevData = prevData,
            compareData = emptyList(),
            showComparison = false,
            rangeLength = rangeLength,
            chartGrouping = appliedChartGrouping,
            chartMode = chartMode,
            durationFormat = durationFormat,
            showSeconds = showSeconds,
            isDarkTheme = isDarkTheme,
        )
        val chartGroupingViewData = statisticsDetailViewDataMapper.mapToChartGroupingViewData(
            availableChartGroupings = availableChartGroupings,
            appliedChartGrouping = appliedChartGrouping,
        )
        val chartLengthViewData = statisticsDetailViewDataMapper.mapToChartLengthViewData(
            availableChartLengths = availableChartLengths,
            appliedChartLength = appliedChartLength,
        )
        val totals = mapTagValuesTotals(
            goalData = data,
            chartValueMode = chartValueMode,
        )

        if (chartData.visible) {
            val mainHint = resourceRepo.getString(R.string.statistics_detail_tag_values_hint)
            val hint = if (valueSuffix.isEmpty()) {
                mainHint
            } else {
                "$mainHint ($valueSuffix)"
            }
            items += StatisticsDetailHintViewData(
                block = StatisticsDetailBlock.TagValuesHint,
                text = hint,
            )
        }

        if (chartData.visible) {
            items += StatisticsDetailBarChartViewData(
                block = StatisticsDetailBlock.TagValuesChartData,
                singleColor = null, // Replaced later.
                marginTopDp = 0,
                data = chartData,
            )
        }

        if (chartGroupingViewData.size > 1) {
            items += ButtonsRowItemViewData(
                block = StatisticsDetailBlock.TagValuesChartGrouping,
                marginTopDp = 4,
                data = chartGroupingViewData,
            )
        }

        if (chartLengthViewData.isNotEmpty()) {
            items += ButtonsRowItemViewData(
                block = StatisticsDetailBlock.TagValuesChartLength,
                marginTopDp = getTopMargin(items),
                data = chartLengthViewData,
            )
        }

        items += mapTagValuesSettingsItem(
            marginTopDp = getTopMargin(items),
            isDarkTheme = isDarkTheme,
        )

        if (rangeAverages.isNotEmpty()) {
            items += StatisticsDetailCardViewData(
                block = StatisticsDetailBlock.TagValuesRangeAverages,
                title = title,
                marginTopDp = 0,
                data = rangeAverages,
            )
        }

        if (chartData.visible) {
            items += StatisticsDetailCardViewData(
                block = StatisticsDetailBlock.TagValuesTotals,
                title = "",
                marginTopDp = 0,
                data = totals,
            )
        }

        return items
    }

    private fun mapTagValuesTotals(
        goalData: List<ChartBarDataDuration>,
        chartValueMode: ChartValueMode,
    ): List<StatisticsDetailCardInternalViewData> {
        val emptyValue by lazy { resourceRepo.getString(R.string.statistics_detail_empty) }

        val barValues = goalData.mapNotNull { bar -> bar.totalDuration }
        val minValue = barValues.minOrNull()?.toFloat()?.div(TAG_VALUE_PRECISION)
        val maxValue = barValues.maxOrNull()?.toFloat()?.div(TAG_VALUE_PRECISION)
        val total = barValues.takeUnless { it.isEmpty() }?.sum()?.toFloat()?.div(TAG_VALUE_PRECISION)

        return listOfNotNull(
            StatisticsDetailCardInternalViewData(
                value = minValue?.toString()?.removeTrailingZeroes() ?: emptyValue,
                valueChange = StatisticsDetailCardInternalViewData.ValueChange.None,
                secondValue = "",
                description = resourceRepo.getString(R.string.records_filter_duration_min),
                titleTextSizeSp = 14,
                subtitleTextSizeSp = 12,
            ),
            StatisticsDetailCardInternalViewData(
                value = total?.toString()?.removeTrailingZeroes() ?: emptyValue,
                valueChange = StatisticsDetailCardInternalViewData.ValueChange.None,
                secondValue = "",
                description = resourceRepo.getString(R.string.statistics_detail_total_duration),
                titleTextSizeSp = 14,
                subtitleTextSizeSp = 12,
            ).takeUnless {
                chartValueMode == ChartValueMode.AVERAGE
            },
            StatisticsDetailCardInternalViewData(
                value = maxValue?.toString()?.removeTrailingZeroes() ?: emptyValue,
                valueChange = StatisticsDetailCardInternalViewData.ValueChange.None,
                secondValue = "",
                description = resourceRepo.getString(R.string.records_filter_duration_max),
                titleTextSizeSp = 14,
                subtitleTextSizeSp = 12,
            ),
        )
    }

    private fun mapTagValuesSettingsItem(
        marginTopDp: Int,
        isDarkTheme: Boolean,
    ): ViewHolderType {
        return StatisticsDetailButtonViewData(
            marginTopDp = marginTopDp,
            data = StatisticsDetailButtonViewData.Button(
                block = StatisticsDetailBlock.TagValuesSettings,
                text = resourceRepo.getString(R.string.shortcut_navigation_settings),
                color = resourceRepo.getThemedAttr(R.attr.appInactiveColor, isDarkTheme),
            ),
            dataSecond = null,
        )
    }

    private fun getTopMargin(currentItems: List<ViewHolderType>): Int {
        // Update margin top depending if has buttons before.
        val hasButtonsBefore = currentItems.lastOrNull() is ButtonsRowItemViewData
        return if (hasButtonsBefore) -10 else 4
    }
}