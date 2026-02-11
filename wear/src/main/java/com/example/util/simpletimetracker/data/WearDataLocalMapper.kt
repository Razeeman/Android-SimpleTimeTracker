/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.data

import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.extension.orFalse
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.model.WearActivity
import com.example.util.simpletimetracker.domain.model.WearCurrentActivity
import com.example.util.simpletimetracker.domain.model.WearCurrentState
import com.example.util.simpletimetracker.domain.model.WearLastRecord
import com.example.util.simpletimetracker.domain.model.WearRecordRepeatResult
import com.example.util.simpletimetracker.domain.model.WearSetSettings
import com.example.util.simpletimetracker.domain.model.WearSettings
import com.example.util.simpletimetracker.domain.model.WearStatistics
import com.example.util.simpletimetracker.domain.model.WearTag
import com.example.util.simpletimetracker.domain.statistics.model.ChartFilterType
import com.example.util.simpletimetracker.wear_api.WearActivityDTO
import com.example.util.simpletimetracker.wear_api.WearChartFilterTypeDTO
import com.example.util.simpletimetracker.wear_api.WearCurrentActivityDTO
import com.example.util.simpletimetracker.wear_api.WearCurrentStateDTO
import com.example.util.simpletimetracker.wear_api.WearDayOfWeekDTO
import com.example.util.simpletimetracker.wear_api.WearLastRecordDTO
import com.example.util.simpletimetracker.wear_api.WearRecordRepeatResponse
import com.example.util.simpletimetracker.wear_api.WearSetSettingsRequest
import com.example.util.simpletimetracker.wear_api.WearSettingsDTO
import com.example.util.simpletimetracker.wear_api.WearStatisticsDTO
import com.example.util.simpletimetracker.wear_api.WearTagDTO
import javax.inject.Inject

class WearDataLocalMapper @Inject constructor() {

    fun map(dto: WearActivityDTO): WearActivity {
        return WearActivity(
            id = dto.id,
            name = dto.name,
            icon = dto.icon,
            color = dto.color,
        )
    }

    fun map(dto: WearCurrentStateDTO): WearCurrentState {
        return WearCurrentState(
            currentActivities = dto.currentActivities.map(::map),
            lastRecords = dto.lastRecords.map(::map),
            suggestionIds = dto.suggestionIds,
        )
    }

    private fun map(dto: WearCurrentActivityDTO): WearCurrentActivity {
        return WearCurrentActivity(
            id = dto.id,
            startedAt = dto.startedAt,
            tags = dto.tags.map(::map),
        )
    }

    fun map(dto: WearStatisticsDTO): WearStatistics {
        return WearStatistics(
            id = dto.id,
            name = dto.name,
            icon = dto.icon,
            color = dto.color,
            duration = dto.duration,
        )
    }

    private fun map(dto: WearLastRecordDTO): WearLastRecord {
        return WearLastRecord(
            activityId = dto.activityId,
            startedAt = dto.startedAt,
            finishedAt = dto.finishedAt,
            tags = dto.tags.map(::map),
        )
    }

    fun map(dto: WearTagDTO): WearTag {
        return WearTag(
            id = dto.id,
            name = dto.name,
            color = dto.color,
            preselected = dto.preselected,
        )
    }

    fun map(dto: WearCurrentActivityDTO.TagDTO): WearCurrentActivity.Tag {
        return WearCurrentActivity.Tag(
            name = dto.name,
            numericValue = dto.numericValue,
            valueSuffix = dto.valueSuffix,
        )
    }

    fun map(dto: WearSettingsDTO): WearSettings {
        return WearSettings(
            apiVersion = dto.apiVersion,
            allowMultitasking = dto.allowMultitasking.orFalse(),
            recordTagSelectionCloseAfterOne = dto.recordTagSelectionCloseAfterOne.orFalse(),
            closeAfterOneTagExcludeActivities = dto.closeAfterOneTagExcludeActivities.orEmpty().toSet(),
            enableRepeatButton = dto.enableRepeatButton.orFalse(),
            retroactiveTrackingMode = dto.retroactiveTrackingMode.orFalse(),
            startOfDayShift = dto.startOfDayShift.orZero(),
            firstDayOfWeek = dto.firstDayOfWeek?.let(::map) ?: DayOfWeek.MONDAY,
        )
    }

    fun map(domain: WearSetSettings): WearSetSettingsRequest {
        return WearSetSettingsRequest(
            allowMultitasking = domain.allowMultitasking,
        )
    }

    fun map(dto: WearRecordRepeatResponse): WearRecordRepeatResult {
        return WearRecordRepeatResult(
            result = when (dto.result) {
                WearRecordRepeatResponse.ActionResult.STARTED ->
                    WearRecordRepeatResult.ActionResult.Started
                WearRecordRepeatResponse.ActionResult.NO_PREVIOUS_FOUND ->
                    WearRecordRepeatResult.ActionResult.NoPreviousFound
                WearRecordRepeatResponse.ActionResult.ALREADY_TRACKING ->
                    WearRecordRepeatResult.ActionResult.AlreadyTracking
            },
        )
    }

    fun map(domain: ChartFilterType): WearChartFilterTypeDTO {
        return when (domain) {
            ChartFilterType.ACTIVITY -> WearChartFilterTypeDTO.ACTIVITY
            ChartFilterType.CATEGORY -> WearChartFilterTypeDTO.CATEGORY
            ChartFilterType.RECORD_TAG -> WearChartFilterTypeDTO.RECORD_TAG
        }
    }

    fun map(dto: WearDayOfWeekDTO): DayOfWeek {
        return when (dto) {
            WearDayOfWeekDTO.SUNDAY -> DayOfWeek.SUNDAY
            WearDayOfWeekDTO.MONDAY -> DayOfWeek.MONDAY
            WearDayOfWeekDTO.TUESDAY -> DayOfWeek.TUESDAY
            WearDayOfWeekDTO.WEDNESDAY -> DayOfWeek.WEDNESDAY
            WearDayOfWeekDTO.THURSDAY -> DayOfWeek.THURSDAY
            WearDayOfWeekDTO.FRIDAY -> DayOfWeek.FRIDAY
            WearDayOfWeekDTO.SATURDAY -> DayOfWeek.SATURDAY
        }
    }
}