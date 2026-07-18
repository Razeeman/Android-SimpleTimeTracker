package com.example.util.simpletimetracker.feature_statistics.interactor

import com.example.util.simpletimetracker.core.interactor.FilterGoalsByDayOfWeekInteractor
import com.example.util.simpletimetracker.core.interactor.StatisticsChartViewDataInteractor
import com.example.util.simpletimetracker.core.interactor.StatisticsMediator
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.GoalViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RangeViewDataMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.base.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.statistics.model.ChartFilterType
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.divider.DividerViewData
import com.example.util.simpletimetracker.feature_statistics.mapper.StatisticsViewDataMapper
import com.example.util.simpletimetracker.feature_statistics.viewData.StatisticsChartViewData
import com.example.util.simpletimetracker.feature_statistics.viewData.StatisticsTitleViewData
import com.example.util.simpletimetracker.feature_views.pieChart.PiePortion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import com.example.util.simpletimetracker.core.mapper.StatisticsViewDataMapper as CoreStatisticsViewDataMapper

class StatisticsViewDataInteractor @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTypeGoalInteractor: RecordTypeGoalInteractor,
    private val statisticsMediator: StatisticsMediator,
    private val statisticsChartViewDataInteractor: StatisticsChartViewDataInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val statisticsViewDataMapper: StatisticsViewDataMapper,
    private val coreStatisticsViewDataMapper: CoreStatisticsViewDataMapper,
    private val rangeViewDataMapper: RangeViewDataMapper,
    private val colorMapper: ColorMapper,
    private val timeMapper: TimeMapper,
    private val goalViewDataMapper: GoalViewDataMapper,
    private val recordInteractor: RecordInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val filterGoalsByDayOfWeekInteractor: FilterGoalsByDayOfWeekInteractor,
) {

    suspend fun getViewData(
        shift: Int,
        forSharing: Boolean,
    ): List<ViewHolderType> = withContext(Dispatchers.Default) {
        val rangeLength = prefsInteractor.getStatisticsRange()
        val filterType = prefsInteractor.getChartFilterType()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val durationFormat = prefsInteractor.getDurationFormat()
        val showSeconds = prefsInteractor.getShowSeconds()
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        val showDuration = rangeLength !is RangeLength.All
        val types = recordTypeInteractor.getAll().associateBy(RecordType::id)
        val showGoalsSeparately = prefsInteractor.getShowGoalsSeparately()

        val filteredIds = when (filterType) {
            ChartFilterType.ACTIVITY -> prefsInteractor.getFilteredTypes()
            ChartFilterType.CATEGORY -> prefsInteractor.getFilteredCategories()
            ChartFilterType.RECORD_TAG -> prefsInteractor.getFilteredTags()
        }

        // Get data.
        val dataHolders = statisticsMediator.getDataHolders(
            filterType = filterType,
            types = types,
        )
        val range = timeMapper.getRangeStartAndEnd(
            rangeLength = rangeLength,
            shift = shift,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
        )
        val statistics = statisticsMediator.getStatistics(
            filterType = filterType,
            filteredIds = filteredIds,
            range = range,
            forceSeconds = false,
        )
        val chart = statisticsChartViewDataInteractor.getChart(
            filterType = filterType,
            filteredIds = filteredIds,
            statistics = statistics,
            dataHolders = dataHolders,
            types = types,
            isDarkTheme = isDarkTheme,
        ).let {
            // If there is no data but have goals - show empty chart.
            val data = it
                .takeUnless { it.isEmpty() }
                ?: PiePortion(
                    id = 0,
                    value = 0,
                    colorInt = colorMapper.toUntrackedColor(isDarkTheme),
                ).let(::listOf)

            StatisticsChartViewData(
                data = data,
                animatedOpen = !forSharing,
            )
        }
        val list = coreStatisticsViewDataMapper.mapItemsList(
            shift = shift,
            filterType = filterType,
            statistics = statistics,
            data = dataHolders,
            filteredIds = filteredIds,
            showDuration = showDuration,
            isDarkTheme = isDarkTheme,
            durationFormat = durationFormat,
            showSeconds = showSeconds,
            hasTransitions = true,
        )
        // Don't show goals in the future if there is no records there.
        val goalsList = if (
            (shift > 0 && statistics.isEmpty()) ||
            showGoalsSeparately
        ) {
            emptyList()
        } else {
            val goals = filterGoalsByDayOfWeekInteractor.execute(
                goals = recordTypeGoalInteractor.getAll(),
                range = range,
                startOfDayShift = startOfDayShift,
            )
            goalViewDataMapper.mapStatisticsList(
                goals = goals,
                types = types,
                filterType = filterType,
                filteredIds = filteredIds,
                rangeLength = rangeLength,
                // TODO goal statistics should be second accurate (forceSeconds).
                statistics = statistics.filterNot { it.id in filteredIds },
                data = dataHolders,
                isDarkTheme = isDarkTheme,
                durationFormat = durationFormat,
                showSeconds = showSeconds,
            )
        }
        val totalTracked: ViewHolderType = statisticsMediator.getStatisticsTotalTracked(
            statistics = statistics,
            filteredIds = filteredIds,
            durationFormat = durationFormat,
            showSeconds = showSeconds,
        ).let {
            statisticsViewDataMapper.mapStatisticsTotalTracked(
                shift = shift,
                totalTracked = it,
                isDarkTheme = isDarkTheme,
            )
        }
        val showFirstEnterHint = when {
            // Show hint ony on current date.
            shift != 0 -> false
            // Check all records only if there is no records for this day.
            list.isNotEmpty() -> false
            // Try to find if any record exists.
            else -> recordInteractor.isEmpty() && runningRecordInteractor.isEmpty()
        }

        // Assemble data.
        val result: MutableList<ViewHolderType> = mutableListOf()

        if (showFirstEnterHint) {
            statisticsViewDataMapper.mapToNoStatistics().let(result::add)
        } else if (list.isEmpty() && goalsList.isEmpty()) {
            statisticsViewDataMapper.mapToEmpty().let(result::add)
        } else {
            if (forSharing) getSharingTitle(rangeLength, shift).let(result::addAll)
            chart.let(result::add)
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

        return@withContext result
    }

    private suspend fun getSharingTitle(
        rangeLength: RangeLength,
        shift: Int,
    ): List<ViewHolderType> = mutableListOf<ViewHolderType>().apply {
        val title = rangeViewDataMapper.mapToShareTitle(
            rangeLength = rangeLength,
            position = shift,
            startOfDayShift = prefsInteractor.getStartOfDayShift(),
            firstDayOfWeek = prefsInteractor.getFirstDayOfWeek(),
        )
        StatisticsTitleViewData(title).let(::add)
        DividerViewData(1).let(::add)
    }
}