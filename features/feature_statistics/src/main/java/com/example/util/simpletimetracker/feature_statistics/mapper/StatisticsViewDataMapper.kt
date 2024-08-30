package com.example.util.simpletimetracker.feature_statistics.mapper

import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.StatisticsDataHolder
import com.example.util.simpletimetracker.domain.UNCATEGORIZED_ITEM_ID
import com.example.util.simpletimetracker.domain.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.mapper.StatisticsMapper
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.domain.model.Statistics
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.empty.EmptyViewData
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_base_adapter.hintBig.HintBigViewData
import com.example.util.simpletimetracker.feature_base_adapter.statistics.StatisticsViewData
import com.example.util.simpletimetracker.feature_statistics.R
import com.example.util.simpletimetracker.feature_statistics.viewData.StatisticsInfoViewData
import com.example.util.simpletimetracker.feature_views.TransitionNames
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import javax.inject.Inject

class StatisticsViewDataMapper @Inject constructor(
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo,
    private val timeMapper: TimeMapper,
    private val statisticsMapper: StatisticsMapper,
) {

    fun mapItemsList(
        shift: Int,
        statistics: List<Statistics>,
        data: Map<Long, StatisticsDataHolder>,
        filterType: ChartFilterType,
        filteredIds: List<Long>,
        showDuration: Boolean,
        isDarkTheme: Boolean,
        useProportionalMinutes: Boolean,
        showSeconds: Boolean,
    ): List<StatisticsViewData> {
        val statisticsFiltered = statistics.filterNot { it.id in filteredIds }
        val sumDuration = statisticsFiltered.sumOf { it.data.duration }
        val statisticsSize = statisticsFiltered.size

        return statisticsFiltered
            .mapNotNull { statistic ->
                val item = mapItem(
                    shift = shift,
                    filterType = filterType,
                    statistics = statistic,
                    sumDuration = sumDuration,
                    dataHolder = data[statistic.id],
                    showDuration = showDuration,
                    isDarkTheme = isDarkTheme,
                    statisticsSize = statisticsSize,
                    useProportionalMinutes = useProportionalMinutes,
                    showSeconds = showSeconds,
                ) ?: return@mapNotNull null

                item to statistic.data.duration
            }
            .sortedByDescending { (_, duration) -> duration }
            .map { (statistics, _) -> statistics }
    }

    fun mapStatisticsTotalTracked(totalTracked: String): ViewHolderType {
        return StatisticsInfoViewData(
            name = resourceRepo.getString(R.string.statistics_total_tracked),
            text = totalTracked,
        )
    }

    fun mapToEmpty(): ViewHolderType {
        return EmptyViewData(
            message = R.string.no_data.let(resourceRepo::getString),
        )
    }

    fun mapToNoStatistics(): ViewHolderType {
        return HintBigViewData(
            text = resourceRepo.getString(R.string.no_statistics_exist),
            infoIconVisible = true,
            closeIconVisible = false,
        )
    }

    fun mapToHint(): ViewHolderType {
        return HintViewData(
            text = R.string.statistics_hint.let(resourceRepo::getString),
        )
    }

    fun mapToGoalHint(): ViewHolderType {
        return HintViewData(
            text = R.string.change_record_type_goal_time_hint.let(resourceRepo::getString),
        )
    }

    fun mapToDailyCalendarHint(): ViewHolderType {
        return HintViewData(
            text = R.string.statistics_daily_calendar_hint.let(resourceRepo::getString),
        )
    }

    private fun mapItem(
        shift: Int,
        filterType: ChartFilterType,
        statistics: Statistics,
        sumDuration: Long,
        dataHolder: StatisticsDataHolder?,
        showDuration: Boolean,
        isDarkTheme: Boolean,
        statisticsSize: Int,
        useProportionalMinutes: Boolean,
        showSeconds: Boolean,
    ): StatisticsViewData? {
        val durationPercent = statisticsMapper.getDurationPercentString(
            sumDuration = sumDuration,
            duration = statistics.data.duration,
            statisticsSize = statisticsSize,
        )
        val transitionName = "${TransitionNames.STATISTICS_DETAIL}_shift${shift}_id${statistics.id}"

        when {
            statistics.id == UNTRACKED_ITEM_ID -> {
                return StatisticsViewData(
                    id = statistics.id,
                    name = R.string.untracked_time_name
                        .let(resourceRepo::getString),
                    duration = mapDuration(
                        statistics = statistics,
                        showDuration = showDuration,
                        showSeconds = showSeconds,
                        useProportionalMinutes = useProportionalMinutes,
                    ),
                    percent = durationPercent,
                    icon = RecordTypeIcon.Image(R.drawable.unknown),
                    color = colorMapper.toUntrackedColor(isDarkTheme),
                    transitionName = transitionName,
                )
            }
            statistics.id == UNCATEGORIZED_ITEM_ID -> {
                return StatisticsViewData(
                    id = statistics.id,
                    name = if (filterType == ChartFilterType.RECORD_TAG) {
                        R.string.change_record_untagged
                    } else {
                        R.string.uncategorized_time_name
                    }.let(resourceRepo::getString),
                    duration = mapDuration(
                        statistics = statistics,
                        showDuration = showDuration,
                        showSeconds = showSeconds,
                        useProportionalMinutes = useProportionalMinutes,
                    ),
                    percent = durationPercent,
                    icon = RecordTypeIcon.Image(R.drawable.untagged),
                    color = colorMapper.toUntrackedColor(isDarkTheme),
                    transitionName = transitionName,
                )
            }
            dataHolder != null -> {
                return StatisticsViewData(
                    id = statistics.id,
                    name = dataHolder.name,
                    duration = mapDuration(
                        statistics = statistics,
                        showDuration = showDuration,
                        showSeconds = showSeconds,
                        useProportionalMinutes = useProportionalMinutes,
                    ),
                    percent = durationPercent,
                    icon = dataHolder.icon
                        ?.let(iconMapper::mapIcon),
                    color = dataHolder.color
                        .let { colorMapper.mapToColorInt(it, isDarkTheme) },
                    transitionName = transitionName,
                )
            }
            else -> {
                return null
            }
        }
    }

    private fun mapDuration(
        statistics: Statistics,
        showDuration: Boolean,
        showSeconds: Boolean,
        useProportionalMinutes: Boolean,
    ): String {
        return if (showDuration) {
            timeMapper.formatInterval(
                interval = statistics.data.duration,
                forceSeconds = showSeconds,
                useProportionalMinutes = useProportionalMinutes,
            )
        } else {
            ""
        }
    }
}