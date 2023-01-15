package com.example.util.simpletimetracker.feature_statistics.interactor

import com.example.util.simpletimetracker.core.interactor.StatisticsChartViewDataInteractor
import com.example.util.simpletimetracker.core.interactor.StatisticsMediator
import com.example.util.simpletimetracker.core.mapper.RangeMapper
import com.example.util.simpletimetracker.core.utils.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.divider.DividerViewData
import com.example.util.simpletimetracker.feature_statistics.mapper.StatisticsViewDataMapper
import com.example.util.simpletimetracker.feature_statistics.viewData.StatisticsChartViewData
import com.example.util.simpletimetracker.feature_statistics.viewData.StatisticsTitleViewData
import javax.inject.Inject

class StatisticsViewDataInteractor @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val statisticsMediator: StatisticsMediator,
    private val statisticsChartViewDataInteractor: StatisticsChartViewDataInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val statisticsViewDataMapper: StatisticsViewDataMapper,
    private val rangeMapper: RangeMapper,
) {

    suspend fun getViewData(
        rangeLength: RangeLength,
        shift: Int,
        forSharing: Boolean,
    ): List<ViewHolderType> {
        val filterType = prefsInteractor.getChartFilterType()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useProportionalMinutes = prefsInteractor.getUseProportionalMinutes()
        val showSeconds = prefsInteractor.getShowSeconds()
        val showDuration = rangeLength !is RangeLength.All
        val types = recordTypeInteractor.getAll()

        val filteredIds = when (filterType) {
            ChartFilterType.ACTIVITY -> prefsInteractor.getFilteredTypes()
            ChartFilterType.CATEGORY -> prefsInteractor.getFilteredCategories()
        }

        // Get data.
        val dataHolders = statisticsMediator.getDataHolders(
            filterType = filterType,
            types = types
        )
        val statistics = statisticsMediator.getStatistics(
            filterType = filterType,
            filteredIds = filteredIds,
            rangeLength = rangeLength,
            shift = shift,
        )
        // Don't show goals in the future if there is no records there.
        val goalsStatistics = if (shift > 0 && statistics.isEmpty()) {
            emptyList()
        } else {
            statisticsMediator.getGoals(
                statistics = statistics,
                rangeLength = rangeLength,
                filterType = filterType,
            )
        }
        // Count running records only for actual period.
        val runningStatistics = if (shift == 0) {
            statisticsMediator.getRunningStatistics()
        } else {
            emptyList()
        }
        val chart = statisticsChartViewDataInteractor.getChart(
            filterType = filterType,
            filteredIds = filteredIds,
            statistics = statistics,
            dataHolders = dataHolders,
            types = types,
            isDarkTheme = isDarkTheme
        ).let {
            StatisticsChartViewData(
                data = it,
                animated = !forSharing,
                buttonsVisible = !forSharing,
            )
        }
        val list = statisticsViewDataMapper.mapItemsList(
            statistics = statistics,
            data = dataHolders,
            filteredIds = filteredIds,
            showDuration = showDuration,
            isDarkTheme = isDarkTheme,
            useProportionalMinutes = useProportionalMinutes,
            showSeconds = showSeconds,
        )
        val goalsList = statisticsViewDataMapper.mapGoalItemsList(
            rangeLength = rangeLength,
            statistics = goalsStatistics,
            runningStatistics = runningStatistics,
            data = dataHolders,
            filteredIds = filteredIds,
            isDarkTheme = isDarkTheme,
            useProportionalMinutes = useProportionalMinutes,
            showSeconds = showSeconds,
        )
        val totalTracked: ViewHolderType = statisticsMediator.getStatisticsTotalTracked(
            statistics = statistics,
            filteredIds = filteredIds,
            useProportionalMinutes = useProportionalMinutes,
            showSeconds = showSeconds,
        ).let(statisticsViewDataMapper::mapStatisticsTotalTracked)

        // Assemble data.
        val result: MutableList<ViewHolderType> = mutableListOf()

        if (list.isEmpty() && goalsList.isEmpty()) {
            statisticsViewDataMapper.mapToEmpty().let(result::add)
        } else {
            if (forSharing) getSharingTitle(rangeLength, shift).let(result::addAll)
            chart.let(result::add) // TODO fix chart in prev dates when there are goals but no records.
            list.let(result::addAll)
            totalTracked.let(result::add)
            // If has any activity or tag other than untracked
            if (list.any { it.id != UNTRACKED_ITEM_ID } && !forSharing) {
                statisticsViewDataMapper.mapToHint().let(result::add)
            }
            if (goalsList.isNotEmpty()) {
                DividerViewData(1).let(result::add)
                statisticsViewDataMapper.mapToGoalHint().let(result::add)
                goalsList.let(result::addAll)
            }
        }

        return result
    }

    private suspend fun getSharingTitle(
        rangeLength: RangeLength,
        shift: Int,
    ): List<ViewHolderType> = mutableListOf<ViewHolderType>().apply {
        val title = rangeMapper.mapToShareTitle(
            rangeLength = rangeLength,
            position = shift,
            startOfDayShift = prefsInteractor.getStartOfDayShift(),
            firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        )
        StatisticsTitleViewData(title).let(::add)
        DividerViewData(1).let(::add)
    }
}