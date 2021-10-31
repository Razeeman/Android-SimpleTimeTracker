package com.example.util.simpletimetracker.feature_statistics.interactor

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.StatisticsMediator
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.domain.model.DayOfWeek
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.Statistics
import com.example.util.simpletimetracker.feature_statistics.mapper.StatisticsViewDataMapper
import com.example.util.simpletimetracker.feature_statistics.viewData.StatisticsDataHolder
import javax.inject.Inject

class StatisticsViewDataInteractor @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val statisticsMediator: StatisticsMediator,
    private val categoryInteractor: CategoryInteractor,
    private val recordTypeCategoryInteractor: RecordTypeCategoryInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val statisticsViewDataMapper: StatisticsViewDataMapper,
    private val timeMapper: TimeMapper,
) {

    suspend fun getViewData(rangeLength: RangeLength, shift: Int): List<ViewHolderType> {
        val filterType = prefsInteractor.getChartFilterType()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val useProportionalMinutes = prefsInteractor.getUseProportionalMinutes()
        val showDuration = rangeLength != RangeLength.ALL
        val types = recordTypeInteractor.getAll()

        val filteredIds = when (filterType) {
            ChartFilterType.ACTIVITY -> prefsInteractor.getFilteredTypes()
            ChartFilterType.CATEGORY -> prefsInteractor.getFilteredCategories()
        }

        // Get data.
        val dataHolders = getDataHolders(filterType, types)
        val statistics = getStatistics(filterType, rangeLength, shift, filteredIds, firstDayOfWeek)
        val chart = getChart(
            filterType, statistics, dataHolders, types, filteredIds, isDarkTheme
        )
        val list = statisticsViewDataMapper.mapItemsList(
            statistics, dataHolders, filteredIds, showDuration, isDarkTheme, useProportionalMinutes
        )
        val totalTracked: ViewHolderType = statisticsViewDataMapper.mapStatisticsTotalTracked(
            statistics, filteredIds, useProportionalMinutes
        )

        // Assemble data.
        val result: MutableList<ViewHolderType> = mutableListOf()

        if (list.isEmpty()) {
            statisticsViewDataMapper.mapToEmpty().let(result::add)
        } else {
            chart.let(result::add)
            list.let(result::addAll)
            totalTracked.let(result::add)
            // If has any activity or tag other than untracked
            if (list.any { it.id != -1L }) {
                statisticsViewDataMapper.mapToHint().let(result::add)
            }
        }

        return result
    }

    private suspend fun getStatistics(
        filterType: ChartFilterType,
        rangeLength: RangeLength,
        shift: Int,
        filteredIds: List<Long>,
        firstDayOfWeek: DayOfWeek,
    ): List<Statistics> {
        val (start, end) = timeMapper.getRangeStartAndEnd(
            rangeLength = rangeLength,
            shift = shift,
            firstDayOfWeek = firstDayOfWeek
        )

        return if (start != 0L && end != 0L) {
            statisticsMediator.getFromRange(
                filterType = filterType,
                start = start,
                end = end,
                addUntracked = !filteredIds.contains(-1L)
            )
        } else {
            statisticsMediator.getAll(
                filterType = filterType
            )
        }
    }

    private suspend fun getDataHolders(
        filterType: ChartFilterType,
        types: List<RecordType>,
    ): Map<Long, StatisticsDataHolder> {
        return when (filterType) {
            ChartFilterType.ACTIVITY -> {
                types.map { type ->
                    type.id to StatisticsDataHolder(
                        name = type.name,
                        color = type.color,
                        icon = type.icon
                    )
                }
            }
            ChartFilterType.CATEGORY -> {
                val categories = categoryInteractor.getAll()
                categories.map { tag ->
                    tag.id to StatisticsDataHolder(
                        name = tag.name,
                        color = tag.color,
                        icon = null
                    )
                }
            }
        }.toMap()
    }

    private suspend fun getChart(
        filterType: ChartFilterType,
        statistics: List<Statistics>,
        dataHolders: Map<Long, StatisticsDataHolder>,
        types: List<RecordType>,
        filteredIds: List<Long>,
        isDarkTheme: Boolean,
    ): ViewHolderType {
        // Add icons for tag chart, use first activity from tag.
        val chartDataHolders: Map<Long, StatisticsDataHolder> = when (filterType) {
            ChartFilterType.ACTIVITY -> {
                dataHolders
            }
            ChartFilterType.CATEGORY -> {
                dataHolders.map { (id, data) ->
                    val typeCategories = recordTypeCategoryInteractor.getAll()
                    val icon = typeCategories
                        .firstOrNull { it.categoryId == id }
                        ?.recordTypeId
                        ?.let { typeId -> types.firstOrNull { it.id == typeId } }
                        ?.icon
                    id to data.copy(icon = icon)
                }.toMap()
            }
        }

        return statisticsViewDataMapper.mapChart(
            statistics = statistics,
            data = chartDataHolders,
            recordTypesFiltered = filteredIds,
            isDarkTheme = isDarkTheme
        )
    }
}