package com.example.util.simpletimetracker.feature_goals.interactor

import com.example.util.simpletimetracker.core.interactor.FilterGoalsByDayOfWeekInteractor
import com.example.util.simpletimetracker.core.interactor.StatisticsMediator
import com.example.util.simpletimetracker.core.mapper.GoalViewDataMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.StatisticsDataHolder
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.empty.EmptyViewData
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_goals.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GoalsViewDataInteractor @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTypeGoalInteractor: RecordTypeGoalInteractor,
    private val statisticsMediator: StatisticsMediator,
    private val prefsInteractor: PrefsInteractor,
    private val goalViewDataMapper: GoalViewDataMapper,
    private val resourceRepo: ResourceRepo,
    private val timeMapper: TimeMapper,
    private val filterGoalsByDayOfWeekInteractor: FilterGoalsByDayOfWeekInteractor,
) {

    suspend fun getViewData(): List<ViewHolderType> = withContext(Dispatchers.Default) {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useProportionalMinutes = prefsInteractor.getUseProportionalMinutes()
        val showSeconds = prefsInteractor.getShowSeconds()
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        val types = recordTypeInteractor.getAll().associateBy(RecordType::id)
        val goals = recordTypeGoalInteractor.getAll()

        val typeDataHolders = statisticsMediator.getDataHolders(
            filterType = ChartFilterType.ACTIVITY,
            types = types,
        )
        val categoryDataHolders = statisticsMediator.getDataHolders(
            filterType = ChartFilterType.CATEGORY,
            types = types,
        )

        val items = goals
            .asSequence()
            .map(RecordTypeGoal::range)
            .toSet()
            .sortedBy {
                when (it) {
                    is RecordTypeGoal.Range.Session -> 0
                    is RecordTypeGoal.Range.Daily -> 1
                    is RecordTypeGoal.Range.Weekly -> 2
                    is RecordTypeGoal.Range.Monthly -> 3
                }
            }
            .mapNotNull {
                when (it) {
                    // No point in statistics for session goals.
                    is RecordTypeGoal.Range.Session -> return@mapNotNull null
                    is RecordTypeGoal.Range.Daily -> RangeLength.Day
                    is RecordTypeGoal.Range.Weekly -> RangeLength.Week
                    is RecordTypeGoal.Range.Monthly -> RangeLength.Month
                }
            }
            .map { rangeLength ->
                val range = timeMapper.getRangeStartAndEnd(
                    rangeLength = rangeLength,
                    shift = 0,
                    firstDayOfWeek = firstDayOfWeek,
                    startOfDayShift = startOfDayShift,
                )
                getViewDataForRange(
                    goals = filterGoalsByDayOfWeekInteractor.execute(
                        goals = goals,
                        range = range,
                        startOfDayShift = startOfDayShift,
                    ),
                    types = types,
                    rangeLength = rangeLength,
                    range = range,
                    typeDataHolders = typeDataHolders,
                    categoryDataHolders = categoryDataHolders,
                    isDarkTheme = isDarkTheme,
                    useProportionalMinutes = useProportionalMinutes,
                    showSeconds = showSeconds,
                )
            }
            .toList()

        return@withContext items
            .flatten()
            .takeUnless { it.isEmpty() }
            ?: mapToEmpty()
    }

    private suspend fun getViewDataForRange(
        goals: List<RecordTypeGoal>,
        types: Map<Long, RecordType>,
        rangeLength: RangeLength,
        range: Range,
        typeDataHolders: Map<Long, StatisticsDataHolder>,
        categoryDataHolders: Map<Long, StatisticsDataHolder>,
        isDarkTheme: Boolean,
        useProportionalMinutes: Boolean,
        showSeconds: Boolean,
    ): List<ViewHolderType> {
        val result = mutableListOf<ViewHolderType>()
        val typeStatistics = statisticsMediator.getStatistics(
            filterType = ChartFilterType.ACTIVITY,
            filteredIds = emptyList(),
            range = range,
        )
        val typeItems = goalViewDataMapper.mapStatisticsList(
            goals = goals,
            types = types,
            filterType = ChartFilterType.ACTIVITY,
            filteredIds = emptyList(),
            rangeLength = rangeLength,
            statistics = typeStatistics,
            data = typeDataHolders,
            isDarkTheme = isDarkTheme,
            useProportionalMinutes = useProportionalMinutes,
            showSeconds = showSeconds,
        )
        val categoryStatistics = statisticsMediator.getStatistics(
            filterType = ChartFilterType.CATEGORY,
            filteredIds = emptyList(),
            range = range,
        )
        val categoryItems = goalViewDataMapper.mapStatisticsList(
            goals = goals,
            types = types,
            filterType = ChartFilterType.CATEGORY,
            filteredIds = emptyList(),
            rangeLength = rangeLength,
            statistics = categoryStatistics,
            data = categoryDataHolders,
            isDarkTheme = isDarkTheme,
            useProportionalMinutes = useProportionalMinutes,
            showSeconds = showSeconds,
        )
        val items = (typeItems + categoryItems)
            .sortedBy { it.goal.percent }

        if (items.isNotEmpty()) {
            val title = when (rangeLength) {
                is RangeLength.Day -> R.string.title_today
                is RangeLength.Week -> R.string.title_this_week
                is RangeLength.Month -> R.string.title_this_month
                else -> return emptyList()
            }.let(resourceRepo::getString)
            HintViewData(title).let(result::add)

            result.addAll(items)
        }

        return result
    }

    private fun mapToEmpty(): List<ViewHolderType> {
        return EmptyViewData(
            message = R.string.no_data.let(resourceRepo::getString),
        ).let(::listOf)
    }
}