package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.viewData.StatisticsDataHolder
import com.example.util.simpletimetracker.domain.UNCATEGORIZED_ITEM_ID
import com.example.util.simpletimetracker.domain.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.extension.getDailyDuration
import com.example.util.simpletimetracker.domain.extension.getMonthlyDuration
import com.example.util.simpletimetracker.domain.extension.getWeeklyDuration
import com.example.util.simpletimetracker.domain.extension.hasDailyDuration
import com.example.util.simpletimetracker.domain.extension.hasMonthlyDuration
import com.example.util.simpletimetracker.domain.extension.hasWeeklyDuration
import com.example.util.simpletimetracker.domain.extension.value
import com.example.util.simpletimetracker.domain.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.StatisticsCategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.StatisticsInteractor
import com.example.util.simpletimetracker.domain.interactor.StatisticsTagInteractor
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.model.Statistics
import javax.inject.Inject

class StatisticsMediator @Inject constructor(
    private val statisticsInteractor: StatisticsInteractor,
    private val statisticsCategoryInteractor: StatisticsCategoryInteractor,
    private val statisticsTagInteractor: StatisticsTagInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val timeMapper: TimeMapper,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val recordTypeGoalInteractor: RecordTypeGoalInteractor,
) {

    suspend fun getStatistics(
        filterType: ChartFilterType,
        filteredIds: List<Long>,
        rangeLength: RangeLength,
        shift: Int,
    ): List<Statistics> {
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()

        val range = timeMapper.getRangeStartAndEnd(
            rangeLength = rangeLength,
            shift = shift,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
        )

        return getFromRange(
            filterType = filterType,
            range = range,
            addUntracked = !filteredIds.contains(UNTRACKED_ITEM_ID),
            addUncategorized = !filteredIds.contains(UNCATEGORIZED_ITEM_ID),
        )
    }

    suspend fun getDataHolders(
        filterType: ChartFilterType,
        types: Map<Long, RecordType>,
        goals: Map<Long, List<RecordTypeGoal>>,
    ): Map<Long, StatisticsDataHolder> {
        return when (filterType) {
            ChartFilterType.ACTIVITY -> {
                types.map { (_, type) ->
                    type.id to StatisticsDataHolder(
                        name = type.name,
                        color = type.color,
                        icon = type.icon,
                        dailyGoalTime = goals[type.id]?.getDailyDuration().value,
                        weeklyGoalTime = goals[type.id]?.getWeeklyDuration().value,
                        monthlyGoalTime = goals[type.id]?.getMonthlyDuration().value,
                    )
                }
            }

            ChartFilterType.CATEGORY -> {
                val categories = categoryInteractor.getAll()
                categories.map { category ->
                    category.id to StatisticsDataHolder(
                        name = category.name,
                        color = category.color,
                        icon = null,
                        dailyGoalTime = 0L,
                        weeklyGoalTime = 0L,
                        monthlyGoalTime = 0L,
                    )
                }
            }

            ChartFilterType.RECORD_TAG -> {
                val tags = recordTagInteractor.getAll()
                tags.map { tag ->
                    val isTyped = tag.typeId != 0L
                    tag.id to StatisticsDataHolder(
                        name = tag.name,
                        color = types[tag.typeId]?.color.takeIf { isTyped } ?: tag.color,
                        icon = types[tag.typeId]?.icon.takeIf { isTyped },
                        dailyGoalTime = 0L,
                        weeklyGoalTime = 0L,
                        monthlyGoalTime = 0L,
                    )
                }
            }
        }.toMap()
    }

    fun getStatisticsTotalTracked(
        statistics: List<Statistics>,
        filteredIds: List<Long>,
        useProportionalMinutes: Boolean,
        showSeconds: Boolean,
    ): String {
        val statisticsFiltered = statistics
            .filterNot { it.id in filteredIds || it.id == UNTRACKED_ITEM_ID }
        val total = statisticsFiltered.map(Statistics::duration).sum()
        return timeMapper.formatInterval(
            interval = total,
            forceSeconds = showSeconds,
            useProportionalMinutes = useProportionalMinutes,
        )
    }

    suspend fun getGoals(
        statistics: List<Statistics>,
        rangeLength: RangeLength,
        filterType: ChartFilterType,
    ): List<Statistics> {
        if (filterType != ChartFilterType.ACTIVITY) {
            return emptyList()
        }
        if (rangeLength !in listOf(RangeLength.Day, RangeLength.Week, RangeLength.Month)) {
            return emptyList()
        }

        val goals = recordTypeGoalInteractor.getAll().groupBy(RecordTypeGoal::typeId)
        return recordTypeInteractor.getAll()
            .filter { type ->
                val typeGoals = goals[type.id].orEmpty()
                when {
                    type.hidden -> false
                    rangeLength is RangeLength.Day -> typeGoals.hasDailyDuration()
                    rangeLength is RangeLength.Week -> typeGoals.hasWeeklyDuration()
                    rangeLength is RangeLength.Month -> typeGoals.hasMonthlyDuration()
                    else -> false
                }
            }.map { type ->
                Statistics(
                    id = type.id,
                    duration = statistics
                        .filter { it.id == type.id }
                        .sumOf(Statistics::duration),
                )
            }
    }

    private suspend fun getFromRange(
        filterType: ChartFilterType,
        range: Range,
        addUntracked: Boolean,
        addUncategorized: Boolean,
    ): List<Statistics> {
        return when (filterType) {
            ChartFilterType.ACTIVITY -> {
                statisticsInteractor.getFromRange(
                    range = range,
                    addUntracked = addUntracked,
                )
            }

            ChartFilterType.CATEGORY -> {
                statisticsCategoryInteractor.getFromRange(
                    range = range,
                    addUntracked = addUntracked,
                    addUncategorized = addUncategorized,
                )
            }

            ChartFilterType.RECORD_TAG -> {
                statisticsTagInteractor.getFromRange(
                    range = range,
                    addUntracked = addUntracked,
                    addUncategorized = addUncategorized,
                )
            }
        }
    }
}