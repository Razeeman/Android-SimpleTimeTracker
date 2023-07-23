package com.example.util.simpletimetracker.feature_statistics.interactor

import com.example.util.simpletimetracker.core.interactor.StatisticsChartViewDataInteractor
import com.example.util.simpletimetracker.core.interactor.StatisticsMediator
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.GoalViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RangeViewDataMapper
import com.example.util.simpletimetracker.domain.UNCATEGORIZED_ITEM_ID
import com.example.util.simpletimetracker.domain.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RecordsFilter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.divider.DividerViewData
import com.example.util.simpletimetracker.feature_statistics.mapper.StatisticsViewDataMapper
import com.example.util.simpletimetracker.feature_statistics.viewData.StatisticsChartViewData
import com.example.util.simpletimetracker.feature_statistics.viewData.StatisticsTitleViewData
import com.example.util.simpletimetracker.feature_views.pieChart.PiePortion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StatisticsViewDataInteractor @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTypeGoalInteractor: RecordTypeGoalInteractor,
    private val statisticsMediator: StatisticsMediator,
    private val statisticsChartViewDataInteractor: StatisticsChartViewDataInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val statisticsViewDataMapper: StatisticsViewDataMapper,
    private val rangeViewDataMapper: RangeViewDataMapper,
    private val colorMapper: ColorMapper,
    private val goalViewDataMapper: GoalViewDataMapper,
    private val recordInteractor: RecordInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
) {

    fun mapFilter(
        filterType: ChartFilterType,
        selectedId: Long,
    ): RecordsFilter {
        if (selectedId == UNTRACKED_ITEM_ID) {
            return RecordsFilter.Untracked
        }

        if (selectedId == UNCATEGORIZED_ITEM_ID) {
            when (filterType) {
                ChartFilterType.CATEGORY -> {
                    return RecordsFilter.CategoryItem.Uncategorized
                        .let(::listOf)
                        .let(RecordsFilter::Category)
                }
                ChartFilterType.RECORD_TAG -> {
                    return RecordsFilter.TagItem.Untagged
                        .let(::listOf)
                        .let(RecordsFilter::SelectedTags)
                }
                ChartFilterType.ACTIVITY -> {
                    // Shouldn't happen normally.
                }
            }
        }

        return when (filterType) {
            ChartFilterType.ACTIVITY -> {
                listOf(selectedId)
                    .let(RecordsFilter::Activity)
            }
            ChartFilterType.CATEGORY -> {
                listOf(selectedId).map(RecordsFilter.CategoryItem::Categorized)
                    .let(RecordsFilter::Category)
            }
            ChartFilterType.RECORD_TAG -> {
                listOf(selectedId).map(RecordsFilter.TagItem::Tagged)
                    .let(RecordsFilter::SelectedTags)
            }
        }
    }

    suspend fun getViewData(
        rangeLength: RangeLength,
        shift: Int,
        forSharing: Boolean,
    ): List<ViewHolderType> = withContext(Dispatchers.Default) {
        val filterType = prefsInteractor.getChartFilterType()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useProportionalMinutes = prefsInteractor.getUseProportionalMinutes()
        val showSeconds = prefsInteractor.getShowSeconds()
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
        val statistics = statisticsMediator.getStatistics(
            filterType = filterType,
            filteredIds = filteredIds,
            rangeLength = rangeLength,
            shift = shift,
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
                    value = 0,
                    colorInt = colorMapper.toUntrackedColor(isDarkTheme),
                ).let(::listOf)

            StatisticsChartViewData(
                data = data,
                animatedOpen = !forSharing,
                buttonsVisible = !forSharing,
            )
        }
        val list = statisticsViewDataMapper.mapItemsList(
            shift = shift,
            filterType = filterType,
            statistics = statistics,
            data = dataHolders,
            filteredIds = filteredIds,
            showDuration = showDuration,
            isDarkTheme = isDarkTheme,
            useProportionalMinutes = useProportionalMinutes,
            showSeconds = showSeconds,
        )
        // Don't show goals in the future if there is no records there.
        val goalsList = if (
            (shift > 0 && statistics.isEmpty()) ||
            showGoalsSeparately
        ) {
            emptyList()
        } else {
            val goals = recordTypeGoalInteractor.getAll()
            goalViewDataMapper.mapStatisticsList(
                goals = goals,
                types = types,
                filterType = filterType,
                rangeLength = rangeLength,
                statistics = statistics.filterNot { it.id in filteredIds },
                data = dataHolders,
                isDarkTheme = isDarkTheme,
                useProportionalMinutes = useProportionalMinutes,
                showSeconds = showSeconds,
            )
        }
        val totalTracked: ViewHolderType = statisticsMediator.getStatisticsTotalTracked(
            statistics = statistics,
            filteredIds = filteredIds,
            useProportionalMinutes = useProportionalMinutes,
            showSeconds = showSeconds,
        ).let(statisticsViewDataMapper::mapStatisticsTotalTracked)
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