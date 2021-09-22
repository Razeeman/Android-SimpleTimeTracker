package com.example.util.simpletimetracker.feature_statistics.mapper

import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.empty.EmptyViewData
import com.example.util.simpletimetracker.core.adapter.hint.HintViewData
import com.example.util.simpletimetracker.core.adapter.statistics.StatisticsViewData
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.RecordTypeIcon
import com.example.util.simpletimetracker.domain.mapper.StatisticsMapper
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RecordTypeCategory
import com.example.util.simpletimetracker.domain.model.Statistics
import com.example.util.simpletimetracker.domain.model.StatisticsCategory
import com.example.util.simpletimetracker.feature_statistics.R
import com.example.util.simpletimetracker.feature_statistics.customView.PiePortion
import com.example.util.simpletimetracker.feature_statistics.viewData.StatisticsChartViewData
import com.example.util.simpletimetracker.feature_statistics.viewData.StatisticsInfoViewData
import javax.inject.Inject

class StatisticsViewDataMapper @Inject constructor(
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo,
    private val timeMapper: TimeMapper,
    private val statisticsMapper: StatisticsMapper
) {

    fun mapActivities(
        statistics: List<Statistics>,
        recordTypes: List<RecordType>,
        recordTypesFiltered: List<Long>,
        showDuration: Boolean,
        isDarkTheme: Boolean,
        useProportionalMinutes: Boolean
    ): List<StatisticsViewData> {
        val statisticsFiltered = statistics.filterNot { it.typeId in recordTypesFiltered }
        val recordTypesMap = recordTypes.map { it.id to it }.toMap()
        val sumDuration = statisticsFiltered.map(Statistics::duration).sum()
        val statisticsSize = statisticsFiltered.size

        return statisticsFiltered
            .mapNotNull { statistic ->
                (
                    mapActivity(
                        statistics = statistic,
                        sumDuration = sumDuration,
                        recordType = recordTypesMap[statistic.typeId],
                        showDuration = showDuration,
                        isDarkTheme = isDarkTheme,
                        statisticsSize = statisticsSize,
                        useProportionalMinutes = useProportionalMinutes,
                    ) ?: return@mapNotNull null
                    ) to statistic.duration
            }
            .sortedByDescending { (_, duration) -> duration }
            .map { (statistics, _) -> statistics }
    }

    fun mapCategories(
        statistics: List<StatisticsCategory>,
        categories: List<Category>,
        categoriesFiltered: List<Long>,
        showDuration: Boolean,
        isDarkTheme: Boolean,
        useProportionalMinutes: Boolean
    ): List<StatisticsViewData> {
        val statisticsFiltered = statistics.filterNot { it.categoryId in categoriesFiltered }
        val categoriesMap = categories.map { it.id to it }.toMap()
        val sumDuration = statisticsFiltered.map(StatisticsCategory::duration).sum()
        val statisticsSize = statisticsFiltered.size

        return statisticsFiltered
            .mapNotNull { statistic ->
                (
                    mapCategory(
                        statistics = statistic,
                        sumDuration = sumDuration,
                        category = categoriesMap[statistic.categoryId],
                        showDuration = showDuration,
                        isDarkTheme = isDarkTheme,
                        statisticsSize = statisticsSize,
                        useProportionalMinutes = useProportionalMinutes,
                    ) ?: return@mapNotNull null
                    ) to statistic.duration
            }
            .sortedByDescending { (_, duration) -> duration }
            .map { (statistics, _) -> statistics }
    }

    fun mapActivitiesToChart(
        statistics: List<Statistics>,
        recordTypes: List<RecordType>,
        recordTypesFiltered: List<Long>,
        isDarkTheme: Boolean
    ): ViewHolderType {
        val recordTypesMap = recordTypes.map { it.id to it }.toMap()

        return StatisticsChartViewData(
            statistics
                .filterNot { it.typeId in recordTypesFiltered }
                .mapNotNull { statistic ->
                    (
                        mapActivityToChart(
                            statistics = statistic,
                            recordType = recordTypesMap[statistic.typeId],
                            isDarkTheme = isDarkTheme
                        ) ?: return@mapNotNull null
                        ) to statistic.duration
                }
                .sortedByDescending { (_, duration) -> duration }
                .map { (statistics, _) -> statistics }
        )
    }

    fun mapCategoriesToChart(
        statisticsCategory: List<StatisticsCategory>,
        categories: List<Category>,
        types: List<RecordType>,
        typeCategories: List<RecordTypeCategory>,
        categoriesFiltered: List<Long>,
        isDarkTheme: Boolean
    ): ViewHolderType {
        val categoriesMap = categories.map { it.id to it }.toMap()

        return StatisticsChartViewData(
            statisticsCategory
                .filterNot { it.categoryId in categoriesFiltered }
                .mapNotNull { statistic ->
                    val type = typeCategories
                        .firstOrNull { it.categoryId == statistic.categoryId }
                        ?.recordTypeId
                        ?.let { typeId -> types.firstOrNull { it.id == typeId } }

                    (
                        mapCategoryToChart(
                            statisticsCategory = statistic,
                            category = categoriesMap[statistic.categoryId],
                            recordType = type,
                            isDarkTheme = isDarkTheme
                        ) ?: return@mapNotNull null
                        ) to statistic.duration
                }
                .sortedByDescending { (_, duration) -> duration }
                .map { (statistics, _) -> statistics }
        )
    }

    // TODO statistics into sealed class and simplify
    fun mapActivitiesTotalTracked(
        statistics: List<Statistics>,
        recordTypesFiltered: List<Long>,
        useProportionalMinutes: Boolean
    ): ViewHolderType {
        val statisticsFiltered = statistics
            .filterNot { it.typeId in recordTypesFiltered || it.typeId == -1L }
        val totalTracked = statisticsFiltered.map(Statistics::duration).sum()

        return mapTotalTracked(totalTracked, useProportionalMinutes)
    }

    fun mapCategoriesTotalTracked(
        statistics: List<StatisticsCategory>,
        categoriesFiltered: List<Long>,
        useProportionalMinutes: Boolean
    ): ViewHolderType {
        val statisticsFiltered = statistics
            .filterNot { it.categoryId in categoriesFiltered || it.categoryId == -1L }
        val totalTracked = statisticsFiltered.map(StatisticsCategory::duration).sum()

        return mapTotalTracked(totalTracked, useProportionalMinutes)
    }

    fun mapToEmpty(): ViewHolderType {
        return EmptyViewData(
            message = R.string.statistics_empty.let(resourceRepo::getString)
        )
    }

    fun mapToHint(): ViewHolderType {
        return HintViewData(
            text = R.string.statistics_hint.let(resourceRepo::getString)
        )
    }

    private fun mapActivity(
        statistics: Statistics,
        sumDuration: Long,
        recordType: RecordType?,
        showDuration: Boolean,
        isDarkTheme: Boolean,
        statisticsSize: Int,
        useProportionalMinutes: Boolean
    ): StatisticsViewData? {
        val durationPercent = statisticsMapper.getDurationPercentString(
            sumDuration = sumDuration,
            duration = statistics.duration,
            statisticsSize = statisticsSize
        )

        when {
            statistics.typeId == -1L -> {
                return StatisticsViewData.Activity(
                    id = statistics.typeId,
                    name = R.string.untracked_time_name
                        .let(resourceRepo::getString),
                    duration = statistics.duration
                        .let { timeMapper.formatInterval(it, useProportionalMinutes) },
                    percent = durationPercent,
                    icon = RecordTypeIcon.Image(R.drawable.unknown),
                    color = colorMapper.toUntrackedColor(isDarkTheme)
                )
            }
            recordType != null -> {
                return StatisticsViewData.Activity(
                    id = statistics.typeId,
                    name = recordType.name,
                    duration = if (showDuration) {
                        statistics.duration.let{ timeMapper.formatInterval(it, useProportionalMinutes) }
                    } else {
                        ""
                    },
                    percent = durationPercent,
                    icon = recordType.icon
                        .let(iconMapper::mapIcon),
                    color = recordType.color
                        .let { colorMapper.mapToColorResId(it, isDarkTheme) }
                        .let(resourceRepo::getColor)
                )
            }
            else -> {
                return null
            }
        }
    }

    private fun mapCategory(
        statistics: StatisticsCategory,
        sumDuration: Long,
        category: Category?,
        showDuration: Boolean,
        isDarkTheme: Boolean,
        statisticsSize: Int,
        useProportionalMinutes: Boolean
    ): StatisticsViewData? {
        val durationPercent = statisticsMapper.getDurationPercentString(
            sumDuration = sumDuration,
            duration = statistics.duration,
            statisticsSize = statisticsSize
        )

        return if (category != null) {
            StatisticsViewData.Category(
                id = statistics.categoryId,
                name = category.name,
                duration = if (showDuration) {
                    statistics.duration.let{ timeMapper.formatInterval(it, useProportionalMinutes) }
                } else {
                    ""
                },
                percent = durationPercent,
                color = category.color
                    .let { colorMapper.mapToColorResId(it, isDarkTheme) }
                    .let(resourceRepo::getColor)
            )
        } else {
            null
        }
    }

    private fun mapActivityToChart(
        statistics: Statistics,
        recordType: RecordType?,
        isDarkTheme: Boolean
    ): PiePortion? {
        return when {
            statistics.typeId == -1L -> {
                PiePortion(
                    value = statistics.duration,
                    colorInt = colorMapper.toUntrackedColor(isDarkTheme),
                    iconId = RecordTypeIcon.Image(R.drawable.unknown)
                )
            }
            recordType != null -> {
                PiePortion(
                    value = statistics.duration,
                    colorInt = recordType.color
                        .let { colorMapper.mapToColorResId(it, isDarkTheme) }
                        .let(resourceRepo::getColor),
                    iconId = recordType.icon
                        .let(iconMapper::mapIcon)
                )
            }
            else -> {
                null
            }
        }
    }

    private fun mapCategoryToChart(
        statisticsCategory: StatisticsCategory,
        category: Category?,
        recordType: RecordType?,
        isDarkTheme: Boolean
    ): PiePortion? {
        return if (category != null) {
            PiePortion(
                value = statisticsCategory.duration,
                colorInt = category.color
                    .let { colorMapper.mapToColorResId(it, isDarkTheme) }
                    .let(resourceRepo::getColor),
                iconId = recordType?.icon
                    ?.let(iconMapper::mapIcon)
            )
        } else {
            null
        }
    }

    private fun mapTotalTracked(totalTracked: Long, useProportionalMinutes: Boolean): ViewHolderType {
        return StatisticsInfoViewData(
            name = resourceRepo.getString(R.string.statistics_total_tracked),
            text = totalTracked.let{ timeMapper.formatInterval(it, useProportionalMinutes) }
        )
    }
}