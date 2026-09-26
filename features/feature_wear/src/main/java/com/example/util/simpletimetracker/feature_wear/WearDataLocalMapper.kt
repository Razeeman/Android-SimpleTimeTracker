/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.feature_wear

import com.example.util.simpletimetracker.core.interactor.RecordRepeatInteractor
import com.example.util.simpletimetracker.core.mapper.RecordTagViewDataMapper
import com.example.util.simpletimetracker.core.viewData.StatisticsDataHolder
import com.example.util.simpletimetracker.domain.color.mapper.AppColorMapper
import com.example.util.simpletimetracker.domain.color.model.AppColor
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.record.model.Record
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.record.model.RunningRecord
import com.example.util.simpletimetracker.domain.record.model.Range
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.statistics.model.ChartFilterType
import com.example.util.simpletimetracker.domain.statistics.model.Statistics
import com.example.util.simpletimetracker.wear_api.WearActivityDTO
import com.example.util.simpletimetracker.wear_api.WearChartFilterTypeDTO
import com.example.util.simpletimetracker.wear_api.WearCurrentActivityDTO
import com.example.util.simpletimetracker.wear_api.WearDayOfWeekDTO
import com.example.util.simpletimetracker.wear_api.WearLastRecordDTO
import com.example.util.simpletimetracker.wear_api.WearRecordRepeatResponse
import com.example.util.simpletimetracker.wear_api.WearRecordDTO
import com.example.util.simpletimetracker.wear_api.WearSettingsDTO
import com.example.util.simpletimetracker.wear_api.WearStatisticsDTO
import com.example.util.simpletimetracker.wear_api.WearTagDTO
import kotlin.math.max
import kotlin.math.min
import javax.inject.Inject

class WearDataLocalMapper @Inject constructor(
    private val appColorMapper: AppColorMapper,
    private val recordTagViewDataMapper: RecordTagViewDataMapper,
) {

    fun map(
        recordType: RecordType,
    ): WearActivityDTO {
        return WearActivityDTO(
            id = recordType.id,
            name = recordType.name,
            icon = recordType.icon,
            color = mapColor(recordType.color),
        )
    }

    fun map(
        record: RunningRecord,
        allTags: List<RecordTag>,
    ): WearCurrentActivityDTO {
        return WearCurrentActivityDTO(
            id = record.id,
            startedAt = record.timeStarted,
            tags = mapTags(record, allTags),
        )
    }

    fun map(
        record: Record,
        allTags: List<RecordTag>,
    ): WearLastRecordDTO {
        return WearLastRecordDTO(
            activityId = record.typeId,
            startedAt = record.timeStarted,
            finishedAt = record.timeEnded,
            tags = mapTags(record, allTags),
        )
    }

    fun map(
        recordTag: RecordTag,
        types: Map<Long, RecordType>,
    ): WearTagDTO {
        return WearTagDTO(
            id = recordTag.id,
            name = recordTag.name,
            color = recordTagViewDataMapper.mapColor(
                tag = recordTag,
                types = types,
            ).let(::mapColor),
        )
    }

    fun map(
        recordTag: RecordTag,
        recordTagData: RecordBase.Tag?,
    ): WearCurrentActivityDTO.TagDTO {
        return WearCurrentActivityDTO.TagDTO(
            name = recordTag.name,
            numericValue = recordTagData?.numericValue,
            valueSuffix = recordTag.valueSuffix,
        )
    }

    fun map(
        statistics: Statistics,
        dataHolder: StatisticsDataHolder?,
    ): WearStatisticsDTO {
        return WearStatisticsDTO(
            id = statistics.id,
            name = dataHolder?.name,
            icon = dataHolder?.icon,
            color = dataHolder?.color?.let(::mapColor),
            duration = statistics.data.duration,
        )
    }

    fun mapRecord(
        id: Long,
        type: WearRecordDTO.TypeDTO,
        recordType: RecordType?,
        record: RecordBase,
        tags: List<RecordTag>,
        range: Range,
    ): WearRecordDTO {
        return WearRecordDTO(
            id = id,
            type = type,
            activityId = recordType?.id,
            activityName = recordType?.name,
            activityIcon = recordType?.icon,
            activityColor = recordType?.color?.let(::mapColor),
            startedAt = max(record.timeStarted, range.timeStarted),
            // TODO WEAR do not clamp running records, same as in the main app?
            endedAt = min(record.timeEnded, range.timeEnded),
            tags = mapTags(record, tags),
        )
    }

    fun map(
        apiVersion: String,
        allowMultitasking: Boolean,
        recordTagSelectionCloseAfterOne: Boolean,
        closeAfterOneTagExcludeActivities: List<Long>,
        enableRepeatButton: Boolean,
        retroactiveTrackingMode: Boolean,
        startOfDayShift: Long,
        firstDayOfWeek: DayOfWeek,
        useMilitaryTime: Boolean,
    ): WearSettingsDTO {
        return WearSettingsDTO(
            apiVersion = apiVersion,
            allowMultitasking = allowMultitasking,
            recordTagSelectionCloseAfterOne = recordTagSelectionCloseAfterOne,
            closeAfterOneTagExcludeActivities = closeAfterOneTagExcludeActivities,
            enableRepeatButton = enableRepeatButton,
            retroactiveTrackingMode = retroactiveTrackingMode,
            startOfDayShift = startOfDayShift,
            firstDayOfWeek = map(firstDayOfWeek),
            useMilitaryTime = useMilitaryTime,
        )
    }

    fun map(
        result: RecordRepeatInteractor.ActionResult,
    ): WearRecordRepeatResponse {
        return WearRecordRepeatResponse(
            result = when (result) {
                is RecordRepeatInteractor.ActionResult.Started ->
                    WearRecordRepeatResponse.ActionResult.STARTED
                is RecordRepeatInteractor.ActionResult.NoPreviousFound ->
                    WearRecordRepeatResponse.ActionResult.NO_PREVIOUS_FOUND
                is RecordRepeatInteractor.ActionResult.AlreadyTracking ->
                    WearRecordRepeatResponse.ActionResult.ALREADY_TRACKING
            },
        )
    }

    fun map(
        dto: WearChartFilterTypeDTO,
    ): ChartFilterType {
        return when (dto) {
            WearChartFilterTypeDTO.ACTIVITY -> ChartFilterType.ACTIVITY
            WearChartFilterTypeDTO.CATEGORY -> ChartFilterType.CATEGORY
            WearChartFilterTypeDTO.RECORD_TAG -> ChartFilterType.RECORD_TAG
        }
    }

    fun map(domain: DayOfWeek): WearDayOfWeekDTO {
        return when (domain) {
            DayOfWeek.SUNDAY -> WearDayOfWeekDTO.SUNDAY
            DayOfWeek.MONDAY -> WearDayOfWeekDTO.MONDAY
            DayOfWeek.TUESDAY -> WearDayOfWeekDTO.TUESDAY
            DayOfWeek.WEDNESDAY -> WearDayOfWeekDTO.WEDNESDAY
            DayOfWeek.THURSDAY -> WearDayOfWeekDTO.THURSDAY
            DayOfWeek.FRIDAY -> WearDayOfWeekDTO.FRIDAY
            DayOfWeek.SATURDAY -> WearDayOfWeekDTO.SATURDAY
        }
    }

    private fun mapTags(
        record: RecordBase,
        allTags: List<RecordTag>,
    ): List<WearCurrentActivityDTO.TagDTO> {
        val tagDataMap = record.tags.associateBy { it.tagId }
        return allTags.mapNotNull { tag ->
            if (tag.id !in tagDataMap.keys) return@mapNotNull null
            map(
                recordTag = tag,
                recordTagData = tagDataMap[tag.id],
            )
        }
    }

    private fun mapColor(appColor: AppColor): Long {
        return appColorMapper.mapToColorInt(appColor).toLong()
    }
}