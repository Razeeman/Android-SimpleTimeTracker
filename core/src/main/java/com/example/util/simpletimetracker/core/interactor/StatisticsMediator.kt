package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.mapper.RecordTagViewDataMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.viewData.StatisticsDataHolder
import com.example.util.simpletimetracker.domain.base.DurationFormat
import com.example.util.simpletimetracker.domain.base.UNCATEGORIZED_ITEM_ID
import com.example.util.simpletimetracker.domain.base.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.category.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.statistics.interactor.StatisticsCategoryInteractor
import com.example.util.simpletimetracker.domain.statistics.interactor.StatisticsInteractor
import com.example.util.simpletimetracker.domain.statistics.interactor.StatisticsTagInteractor
import com.example.util.simpletimetracker.domain.statistics.model.ChartFilterType
import com.example.util.simpletimetracker.domain.record.model.Range
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.statistics.model.Statistics
import javax.inject.Inject

class StatisticsMediator @Inject constructor(
    private val statisticsInteractor: StatisticsInteractor,
    private val statisticsCategoryInteractor: StatisticsCategoryInteractor,
    private val statisticsTagInteractor: StatisticsTagInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val timeMapper: TimeMapper,
    private val recordTagInteractor: RecordTagInteractor,
    private val recordTagViewDataMapper: RecordTagViewDataMapper,
) {

    suspend fun getStatistics(
        filterType: ChartFilterType,
        filteredIds: List<Long>,
        range: Range,
        forceSeconds: Boolean,
    ): List<Statistics> {
        val showUntracked = prefsInteractor.getShowUntrackedInStatistics()
        val showSeconds = forceSeconds || prefsInteractor.getShowSeconds()

        return getFromRange(
            filterType = filterType,
            range = range,
            addUntracked = !filteredIds.contains(UNTRACKED_ITEM_ID) && showUntracked,
            addUncategorized = !filteredIds.contains(UNCATEGORIZED_ITEM_ID),
            showSeconds = showSeconds,
        )
    }

    suspend fun getDataHolders(
        filterType: ChartFilterType,
        types: Map<Long, RecordType>,
    ): Map<Long, StatisticsDataHolder> {
        return when (filterType) {
            ChartFilterType.ACTIVITY -> {
                types.map { (_, type) ->
                    type.id to StatisticsDataHolder(
                        name = type.name,
                        color = type.color,
                        icon = type.icon,
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
                    )
                }
            }

            ChartFilterType.RECORD_TAG -> {
                val tags = recordTagInteractor.getAll()
                tags.map { tag ->
                    tag.id to StatisticsDataHolder(
                        name = tag.name,
                        color = recordTagViewDataMapper.mapColor(tag, types),
                        icon = recordTagViewDataMapper.mapIcon(tag, types),
                    )
                }
            }
        }.toMap()
    }

    fun getStatisticsTotalTracked(
        statistics: List<Statistics>,
        filteredIds: List<Long>,
        durationFormat: DurationFormat,
        showSeconds: Boolean,
    ): String {
        val statisticsFiltered = statistics
            .filterNot { it.id in filteredIds || it.id == UNTRACKED_ITEM_ID }
        val total = statisticsFiltered.sumOf { it.data.duration }
        return timeMapper.formatInterval(
            interval = total,
            forceSeconds = showSeconds,
            durationFormat = durationFormat,
        )
    }

    private suspend fun getFromRange(
        filterType: ChartFilterType,
        range: Range,
        addUntracked: Boolean,
        addUncategorized: Boolean,
        showSeconds: Boolean,
    ): List<Statistics> {
        return when (filterType) {
            ChartFilterType.ACTIVITY -> {
                statisticsInteractor.getFromRange(
                    range = range,
                    addUntracked = addUntracked,
                    showSeconds = showSeconds,
                )
            }

            ChartFilterType.CATEGORY -> {
                statisticsCategoryInteractor.getFromRange(
                    range = range,
                    addUntracked = addUntracked,
                    addUncategorized = addUncategorized,
                    showSeconds = showSeconds,
                )
            }

            ChartFilterType.RECORD_TAG -> {
                statisticsTagInteractor.getFromRange(
                    range = range,
                    addUntracked = addUntracked,
                    addUncategorized = addUncategorized,
                    showSeconds = showSeconds,
                )
            }
        }
    }
}