package com.example.util.simpletimetracker.feature_goals.interactor

import com.example.util.simpletimetracker.core.interactor.StatisticsMediator
import com.example.util.simpletimetracker.core.mapper.GoalViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.StatisticsDataHolder
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.ChartFilterType
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
) {

    suspend fun getViewData(): List<ViewHolderType> = withContext(Dispatchers.Default) {
        val filterType = prefsInteractor.getChartFilterType()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useProportionalMinutes = prefsInteractor.getUseProportionalMinutes()
        val showSeconds = prefsInteractor.getShowSeconds()
        val types = recordTypeInteractor.getAll().associateBy(RecordType::id)
        val goals = recordTypeGoalInteractor.getAll()
        val goalsMap = goals.groupBy(RecordTypeGoal::typeId)

        val dataHolders = statisticsMediator.getDataHolders(
            filterType = filterType,
            types = types,
            goals = goalsMap,
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
                getViewDataForRange(
                    filterType = filterType,
                    rangeLength = rangeLength,
                    dataHolders = dataHolders,
                    isDarkTheme = isDarkTheme,
                    useProportionalMinutes = useProportionalMinutes,
                    showSeconds = showSeconds,
                )
            }
            .toList()


        return@withContext items
            .takeUnless { it.isEmpty() }
            ?.flatten()
            ?: mapToEmpty()
    }

    private suspend fun getViewDataForRange(
        filterType: ChartFilterType,
        rangeLength: RangeLength,
        dataHolders: Map<Long, StatisticsDataHolder>,
        isDarkTheme: Boolean,
        useProportionalMinutes: Boolean,
        showSeconds: Boolean,
    ): List<ViewHolderType> {
        val statistics = statisticsMediator.getStatistics(
            filterType = filterType,
            filteredIds = emptyList(),
            rangeLength = rangeLength,
            shift = 0,
        )
        val goalsStatistics = statisticsMediator.getGoals(
            statistics = statistics,
            rangeLength = rangeLength,
            filterType = filterType,
        )
        val result = mutableListOf<ViewHolderType>()
        val items = goalViewDataMapper.mapList(
            rangeLength = rangeLength,
            statistics = goalsStatistics,
            data = dataHolders,
            isDarkTheme = isDarkTheme,
            useProportionalMinutes = useProportionalMinutes,
            showSeconds = showSeconds,
        )

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
            message = R.string.records_empty.let(resourceRepo::getString),
        ).let(::listOf)
    }
}