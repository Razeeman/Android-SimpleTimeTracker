/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.features.statistics.mapper

import androidx.compose.ui.graphics.toArgb
import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.core.ErrorStateMapper
import com.example.util.simpletimetracker.core.mapper.RangeTitleMapper
import com.example.util.simpletimetracker.core.mapper.StatisticsMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.data.WearIconMapper
import com.example.util.simpletimetracker.data.WearResourceRepo
import com.example.util.simpletimetracker.domain.base.DurationFormat
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.model.WearActivityIcon
import com.example.util.simpletimetracker.domain.model.WearSettings
import com.example.util.simpletimetracker.domain.model.WearStatistics
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.features.statistics.screen.StatisticsListState
import com.example.util.simpletimetracker.features.statistics.ui.StatisticsChipState
import com.example.util.simpletimetracker.presentation.theme.ColorInactive
import javax.inject.Inject

class StatisticsViewDataMapper @Inject constructor(
    private val wearIconMapper: WearIconMapper,
    private val resourceRepo: WearResourceRepo,
    private val timeMapper: TimeMapper,
    private val rangeTitleMapper: RangeTitleMapper,
    private val statisticsMapper: StatisticsMapper,
    private val errorStateMapper: ErrorStateMapper,
) {

    fun mapErrorState(): StatisticsListState.Error {
        return StatisticsListState.Error(errorStateMapper.map())
    }

    fun mapEmptyState(
        rangeLength: RangeLength,
        shift: Int,
        settings: WearSettings?,
    ): StatisticsListState.Empty {
        return StatisticsListState.Empty(
            title = mapToTitle(
                rangeLength = rangeLength,
                shift = shift,
                settings = settings,
            ),
            messageResId = R.string.no_data,
        )
    }

    fun mapContentLoadingState(
        rangeLength: RangeLength,
        shift: Int,
        settings: WearSettings?,
    ): StatisticsListState.Content {
        return StatisticsListState.Content(
            title = mapToTitle(
                rangeLength = rangeLength,
                shift = shift,
                settings = settings,
            ),
            items = listOf(StatisticsListState.Content.Item.Loader),
        )
    }

    fun mapContentState(
        statistics: List<WearStatistics>,
        rangeLength: RangeLength,
        shift: Int,
        settings: WearSettings?,
    ): StatisticsListState.Content {
        val items = mutableListOf<StatisticsListState.Content.Item>()

        val sumDuration = statistics.sumOf { it.duration }
        val statisticsSize = statistics.size

        items += statistics
            .mapNotNull { statistic ->
                val item = mapItem(
                    statistics = statistic,
                    sumDuration = sumDuration,
                    statisticsSize = statisticsSize,
                ) ?: return@mapNotNull null
                item to statistic.duration
            }
            .sortedByDescending { (_, duration) -> duration }
            .map { (statistics, _) ->
                StatisticsListState.Content.Item.Statistics(statistics)
            }

        items += mapTotalItem(
            total = getStatisticsTotalTracked(statistics),
        ).let {
            StatisticsListState.Content.Item.Total(it)
        }

        val title = mapToTitle(
            rangeLength = rangeLength,
            shift = shift,
            settings = settings,
        )

        return StatisticsListState.Content(
            title = title,
            items = items,
        )
    }

    private fun mapItem(
        statistics: WearStatistics,
        sumDuration: Long,
        statisticsSize: Int,
    ): StatisticsChipState? {
        val durationPercent = statisticsMapper.getDurationPercentString(
            sumDuration = sumDuration,
            duration = statistics.duration,
            statisticsSize = statisticsSize,
        )
        val duration = timeMapper.formatInterval(
            interval = statistics.duration,
            forceSeconds = true,
            durationFormat = DurationFormat.HOURS,
        )

        return when (statistics.type) {
            WearStatistics.Type.Untracked -> {
                StatisticsChipState(
                    id = statistics.id,
                    name = resourceRepo.getString(R.string.untracked_time_name),
                    icon = WearActivityIcon.Image(R.drawable.wear_unknown),
                    color = ColorInactive.toArgb().toLong(),
                    duration = duration,
                    percent = durationPercent,
                )
            }
            WearStatistics.Type.Uncategorized,
            WearStatistics.Type.Untagged,
            -> {
                StatisticsChipState(
                    id = statistics.id,
                    name = if (statistics.type == WearStatistics.Type.Untagged) {
                        R.string.change_record_untagged
                    } else {
                        R.string.uncategorized_time_name
                    }.let(resourceRepo::getString),
                    icon = WearActivityIcon.Image(R.drawable.wear_untagged),
                    color = ColorInactive.toArgb().toLong(),
                    duration = duration,
                    percent = durationPercent,
                )
            }
            WearStatistics.Type.Activity,
            WearStatistics.Type.Category,
            WearStatistics.Type.Tag,
            -> if (statistics.name != null) {
                StatisticsChipState(
                    id = statistics.id,
                    name = statistics.name,
                    icon = statistics.icon?.let(wearIconMapper::mapIcon),
                    color = statistics.color ?: ColorInactive.toArgb().toLong(),
                    duration = duration,
                    percent = durationPercent,
                )
            } else {
                null
            }
        }
    }

    private fun mapTotalItem(
        total: String,
    ): StatisticsChipState {
        return StatisticsChipState(
            id = 0,
            name = resourceRepo.getString(R.string.statistics_total_tracked_short),
            icon = null,
            color = ColorInactive.toArgb().toLong(),
            duration = total,
            percent = null,
        )
    }

    private fun getStatisticsTotalTracked(
        statistics: List<WearStatistics>,
    ): String {
        val statisticsFiltered = statistics
            .filterNot { it.type == WearStatistics.Type.Untracked }
        val total = statisticsFiltered.sumOf { it.duration }
        return timeMapper.formatInterval(
            interval = total,
            forceSeconds = true,
            durationFormat = DurationFormat.HOURS,
        )
    }

    private fun mapToTitle(
        rangeLength: RangeLength,
        shift: Int,
        settings: WearSettings?,
    ): String {
        return rangeTitleMapper.mapToTitle(
            rangeLength = rangeLength,
            position = shift,
            startOfDayShift = settings?.startOfDayShift.orZero(),
            firstDayOfWeek = settings?.firstDayOfWeek ?: DayOfWeek.MONDAY,
        )
    }
}