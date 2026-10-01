/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.features.records.mapper

import androidx.compose.ui.graphics.toArgb
import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.core.ErrorStateMapper
import com.example.util.simpletimetracker.core.mapper.RecordTagValueMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.data.WearIconMapper
import com.example.util.simpletimetracker.data.WearResourceRepo
import com.example.util.simpletimetracker.domain.base.DurationFormat
import com.example.util.simpletimetracker.domain.extension.orFalse
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.model.WearActivityIcon
import com.example.util.simpletimetracker.domain.model.WearRecord
import com.example.util.simpletimetracker.domain.model.WearSettings
import com.example.util.simpletimetracker.features.records.screen.RecordsListState
import com.example.util.simpletimetracker.features.records.ui.RecordChipState
import com.example.util.simpletimetracker.presentation.theme.ColorInactive
import javax.inject.Inject

class RecordsViewDataMapper @Inject constructor(
    private val wearIconMapper: WearIconMapper,
    private val resourceRepo: WearResourceRepo,
    private val timeMapper: TimeMapper,
    private val recordTagValueMapper: RecordTagValueMapper,
    private val errorStateMapper: ErrorStateMapper,
) {

    fun mapErrorState(): RecordsListState.Error {
        return RecordsListState.Error(errorStateMapper.map())
    }

    fun mapEmptyState(
        shift: Int,
        settings: WearSettings?,
    ): RecordsListState.Empty {
        return RecordsListState.Empty(
            title = mapTitle(
                shift = shift,
                settings = settings,
            ),
            messageResId = R.string.no_data,
        )
    }

    fun mapContentLoadingState(shift: Int, settings: WearSettings?): RecordsListState.Content {
        return RecordsListState.Content(
            title = mapTitle(
                shift = shift,
                settings = settings,
            ),
            items = emptyList(),
            isLoading = true,
        )
    }

    fun mapContentState(
        records: List<WearRecord>,
        shift: Int,
        settings: WearSettings?,
        now: Long,
    ): RecordsListState.Content {
        return RecordsListState.Content(
            title = mapTitle(
                shift = shift,
                settings = settings,
            ),
            items = records.map {
                mapItem(
                    record = it,
                    settings = settings,
                    now = now,
                )
            },
            isLoading = false,
        )
    }

    private fun mapItem(
        record: WearRecord,
        settings: WearSettings?,
        now: Long,
    ): RecordChipState {
        val useMilitaryTime = settings?.useMilitaryTime.orFalse()
        val end = when (record.type) {
            WearRecord.Type.Running,
            -> now
            WearRecord.Type.Tracked,
            WearRecord.Type.Untracked,
            -> record.endedAt
        }
        val durationMillis = (end - record.startedAt).coerceAtLeast(0L)
        val startTime = timeMapper.formatTime(
            time = record.startedAt,
            useMilitaryTime = useMilitaryTime,
            // TODO WEAR chip has small width, both start end with seconds may not fit.
            showSeconds = false,
        )
        val endTime = timeMapper.formatTime(
            time = end,
            useMilitaryTime = useMilitaryTime,
            showSeconds = false,
        )
        val timeText = when (record.type) {
            WearRecord.Type.Running,
            -> startTime
            WearRecord.Type.Tracked,
            WearRecord.Type.Untracked,
            -> "$startTime – $endTime"
        }

        return RecordChipState(
            key = "${record.type}:${record.id}",
            name = record.activityName
                ?: resourceRepo.getString(R.string.untracked_time_name),
            icon = record.activityIcon
                ?.let(wearIconMapper::mapIcon)
                ?: WearActivityIcon.Image(R.drawable.wear_unknown),
            color = record.activityColor
                ?: ColorInactive.toArgb().toLong(),
            tags = record.tags.joinToString(", ") { tag ->
                tag.numericValue?.let { value ->
                    recordTagValueMapper.getNameWithValue(
                        name = tag.name,
                        value = value,
                        valueSuffix = tag.valueSuffix.orEmpty(),
                    )
                } ?: tag.name
            },
            time = timeText,
            duration = timeMapper.formatInterval(
                interval = durationMillis,
                forceSeconds = true,
                durationFormat = DurationFormat.HOURS,
            ),
            isRunning = record.type == WearRecord.Type.Running,
            isUntracked = record.type == WearRecord.Type.Untracked,
        )
    }

    private fun mapTitle(shift: Int, settings: WearSettings?): String {
        return timeMapper.toDayTitle(
            daysFromToday = shift,
            startOfDayShift = settings?.startOfDayShift.orZero(),
        )
    }
}
