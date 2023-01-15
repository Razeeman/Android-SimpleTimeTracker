package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.utils.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.core.viewData.StatisticsDataHolder
import com.example.util.simpletimetracker.domain.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.StatisticsCategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.StatisticsInteractor
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.Statistics
import javax.inject.Inject

class StatisticsMediator @Inject constructor(
    private val statisticsInteractor: StatisticsInteractor,
    private val statisticsCategoryInteractor: StatisticsCategoryInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val timeMapper: TimeMapper,
    private val recordTypeInteractor: RecordTypeInteractor,
) {

    suspend fun getStatistics(
        filterType: ChartFilterType,
        filteredIds: List<Long>,
        rangeLength: RangeLength,
        shift: Int,
    ): List<Statistics> {
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()

        val (start, end) = timeMapper.getRangeStartAndEnd(
            rangeLength = rangeLength,
            shift = shift,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
        )

        return if (start != 0L && end != 0L) {
            getFromRange(
                filterType = filterType,
                start = start,
                end = end,
                addUntracked = !filteredIds.contains(UNTRACKED_ITEM_ID)
            )
        } else {
            getAll(
                filterType = filterType
            )
        }
    }

    suspend fun getRunningStatistics(): List<Statistics> {
        return statisticsInteractor.getAllRunning()
    }

    suspend fun getDataHolders(
        filterType: ChartFilterType,
        types: List<RecordType>,
    ): Map<Long, StatisticsDataHolder> {
        return when (filterType) {
            ChartFilterType.ACTIVITY -> {
                types.map { type ->
                    type.id to StatisticsDataHolder(
                        name = type.name,
                        color = type.color,
                        icon = type.icon,
                        dailyGoalTime = type.dailyGoalTime,
                        weeklyGoalTime = type.weeklyGoalTime,
                        monthlyGoalTime = type.monthlyGoalTime,
                    )
                }
            }
            ChartFilterType.CATEGORY -> {
                val categories = categoryInteractor.getAll()
                categories.map { tag ->
                    tag.id to StatisticsDataHolder(
                        name = tag.name,
                        color = tag.color,
                        icon = null,
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

        return recordTypeInteractor.getAll()
            .filter { type ->
                when {
                    type.hidden -> false
                    rangeLength is RangeLength.Day -> type.dailyGoalTime > 0L
                    rangeLength is RangeLength.Week -> type.weeklyGoalTime > 0L
                    rangeLength is RangeLength.Month -> type.monthlyGoalTime > 0L
                    else -> false
                }
            }.map { type ->
                Statistics(
                    id = type.id,
                    duration = statistics
                        .filter { it.id == type.id }
                        .sumOf { it.duration }
                )
            }
    }

    private suspend fun getAll(
        filterType: ChartFilterType,
    ): List<Statistics> {
        return when (filterType) {
            ChartFilterType.ACTIVITY -> statisticsInteractor.getAll()
            ChartFilterType.CATEGORY -> statisticsCategoryInteractor.getAll()
        }
    }

    private suspend fun getFromRange(
        filterType: ChartFilterType,
        start: Long,
        end: Long,
        addUntracked: Boolean,
    ): List<Statistics> {
        return when (filterType) {
            ChartFilterType.ACTIVITY -> statisticsInteractor.getFromRange(start, end, addUntracked)
            ChartFilterType.CATEGORY -> statisticsCategoryInteractor.getFromRange(start, end)
        }
    }
}