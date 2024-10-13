/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.feature_wear

import com.example.util.simpletimetracker.core.interactor.RecordRepeatInteractor
import com.example.util.simpletimetracker.core.mapper.RecordTagViewDataMapper
import com.example.util.simpletimetracker.domain.mapper.AppColorMapper
import com.example.util.simpletimetracker.domain.model.AppColor
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.wear_api.WearActivityDTO
import com.example.util.simpletimetracker.wear_api.WearCurrentActivityDTO
import com.example.util.simpletimetracker.wear_api.WearRecordRepeatResponse
import com.example.util.simpletimetracker.wear_api.WearSettingsDTO
import com.example.util.simpletimetracker.wear_api.WearTagDTO
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
        tags: List<WearTagDTO>,
    ): WearCurrentActivityDTO {
        return WearCurrentActivityDTO(
            id = record.id,
            startedAt = record.timeStarted,
            tags = tags,
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
        allowMultitasking: Boolean,
        recordTagSelectionCloseAfterOne: Boolean,
        enableRepeatButton: Boolean,
    ): WearSettingsDTO {
        return WearSettingsDTO(
            allowMultitasking = allowMultitasking,
            recordTagSelectionCloseAfterOne = recordTagSelectionCloseAfterOne,
            enableRepeatButton = enableRepeatButton,
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

    private fun mapColor(appColor: AppColor): Long {
        return appColorMapper.mapToColorInt(appColor).toLong()
    }
}