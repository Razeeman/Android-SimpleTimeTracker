package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.utils.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.core.viewData.StatisticsDataHolder
import com.example.util.simpletimetracker.domain.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.Statistics
import com.example.util.simpletimetracker.feature_views.pieChart.PiePortion
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import javax.inject.Inject

class StatisticsChartViewDataInteractor @Inject constructor(
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo,
    private val recordTypeCategoryInteractor: RecordTypeCategoryInteractor,
) {

    suspend fun getChart(
        filterType: ChartFilterType,
        filteredIds: List<Long>,
        statistics: List<Statistics>,
        dataHolders: Map<Long, StatisticsDataHolder>,
        types: List<RecordType>,
        isDarkTheme: Boolean,
    ): List<PiePortion> {
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

        return mapChart(
            statistics = statistics,
            data = chartDataHolders,
            recordTypesFiltered = filteredIds,
            isDarkTheme = isDarkTheme
        )
    }

    private fun mapChart(
        statistics: List<Statistics>,
        data: Map<Long, StatisticsDataHolder>,
        recordTypesFiltered: List<Long>,
        isDarkTheme: Boolean,
    ): List<PiePortion> {
        return statistics
            .filterNot { it.id in recordTypesFiltered }
            .mapNotNull { statistic ->
                (
                    mapChart(
                        statistics = statistic,
                        dataHolder = data[statistic.id],
                        isDarkTheme = isDarkTheme
                    ) ?: return@mapNotNull null
                    ) to statistic.duration
            }
            .sortedByDescending { (_, duration) -> duration }
            .map { (statistics, _) -> statistics }
    }

    private fun mapChart(
        statistics: Statistics,
        dataHolder: StatisticsDataHolder?,
        isDarkTheme: Boolean,
    ): PiePortion? {
        return when {
            statistics.id == UNTRACKED_ITEM_ID -> {
                PiePortion(
                    value = statistics.duration,
                    colorInt = colorMapper.toUntrackedColor(isDarkTheme),
                    iconId = RecordTypeIcon.Image(R.drawable.unknown)
                )
            }
            dataHolder != null -> {
                PiePortion(
                    value = statistics.duration,
                    colorInt = dataHolder.color
                        .let { colorMapper.mapToColorResId(it, isDarkTheme) }
                        .let(resourceRepo::getColor),
                    iconId = dataHolder.icon
                        ?.let(iconMapper::mapIcon)
                )
            }
            else -> {
                null
            }
        }
    }
}