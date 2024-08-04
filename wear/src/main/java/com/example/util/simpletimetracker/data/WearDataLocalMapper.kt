/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.data

import com.example.util.simpletimetracker.domain.model.WearActivity
import com.example.util.simpletimetracker.domain.model.WearCurrentActivity
import com.example.util.simpletimetracker.domain.model.WearSettings
import com.example.util.simpletimetracker.domain.model.WearTag
import com.example.util.simpletimetracker.wear_api.WearActivityDTO
import com.example.util.simpletimetracker.wear_api.WearCurrentActivityDTO
import com.example.util.simpletimetracker.wear_api.WearSettingsDTO
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

    fun map(dto: WearCurrentActivityDTO): WearCurrentActivity {
        return WearCurrentActivity(
            id = dto.id,
            startedAt = dto.startedAt,
            tags = dto.tags.map(::map),
        )
    }

    fun map(dto: WearTagDTO): WearTag {
        return WearTag(
            id = dto.id,
            name = dto.name,
            color = dto.color,
        )
    }

    fun map(dto: WearSettingsDTO): WearSettings {
        return WearSettings(
            allowMultitasking = dto.allowMultitasking,
            recordTagSelectionCloseAfterOne = dto.recordTagSelectionCloseAfterOne,
        )
    }

    fun map(domain: WearSettings): WearSettingsDTO {
        return WearSettingsDTO(
            allowMultitasking = domain.allowMultitasking,
            recordTagSelectionCloseAfterOne = domain.recordTagSelectionCloseAfterOne,
        )
    }
}