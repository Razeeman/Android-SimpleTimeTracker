/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.screens.activities

import androidx.compose.ui.graphics.toArgb
import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.data.WearIconMapper
import com.example.util.simpletimetracker.data.WearResourceRepo
import com.example.util.simpletimetracker.domain.model.WearActivity
import com.example.util.simpletimetracker.domain.model.WearActivityIcon
import com.example.util.simpletimetracker.domain.model.WearCurrentActivity
import com.example.util.simpletimetracker.domain.model.WearSettings
import com.example.util.simpletimetracker.presentation.theme.ColorInactive
import com.example.util.simpletimetracker.presentation.ui.components.ActivitiesListState
import com.example.util.simpletimetracker.presentation.ui.components.ActivityChipState
import com.example.util.simpletimetracker.presentation.ui.components.ActivityChipType
import javax.inject.Inject

class ActivitiesViewDataMapper @Inject constructor(
    private val wearIconMapper: WearIconMapper,
    private val resourceRepo: WearResourceRepo,
) {

    fun mapErrorState(): ActivitiesListState.Error {
        return ActivitiesListState.Error(R.string.wear_loading_error)
    }

    fun mapEmptyState(): ActivitiesListState.Empty {
        return ActivitiesListState.Empty(R.string.record_types_empty)
    }

    fun mapContentState(
        activities: List<WearActivity>,
        currentActivities: List<WearCurrentActivity>,
        settings: WearSettings?,
        showCompactList: Boolean,
    ): ActivitiesListState.Content {
        val currentActivitiesMap = currentActivities.associateBy { it.id }
        val items = mutableListOf<ActivityChipState>()

        if (settings?.enableRepeatButton == true) {
            items += mapRepeatItem()
        }
        items += activities.map { activity ->
            mapItem(
                activity = activity,
                currentActivity = currentActivitiesMap[activity.id],
            )
        }

        return ActivitiesListState.Content(
            isCompact = showCompactList,
            items = items,
        )
    }

    private fun mapItem(
        activity: WearActivity,
        currentActivity: WearCurrentActivity?,
    ): ActivityChipState {
        val tagString = currentActivity?.tags
            .orEmpty()
            .map { it.name }
            .takeUnless { it.isEmpty() }
            ?.joinToString(separator = ", ")
            .orEmpty()
        val icon = wearIconMapper.mapIcon(activity.icon)

        return ActivityChipState(
            id = activity.id,
            name = activity.name,
            icon = icon,
            color = activity.color,
            type = ActivityChipType.Base,
            startedAt = currentActivity?.startedAt,
            tagString = tagString,
        )
    }

    private fun mapRepeatItem(): ActivityChipState {
        return ActivityChipState(
            id = 0L,
            name = resourceRepo.getString(R.string.running_records_repeat),
            icon = WearActivityIcon.Image(R.drawable.wear_repeat),
            color = ColorInactive.toArgb().toLong(),
            type = ActivityChipType.Repeat,
        )
    }
}