/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.screens.activities

import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.data.WearIconMapper
import com.example.util.simpletimetracker.presentation.components.ActivitiesListState
import com.example.util.simpletimetracker.presentation.components.ActivityChipState
import com.example.util.simpletimetracker.wear_api.WearActivity
import com.example.util.simpletimetracker.wear_api.WearCurrentActivity
import javax.inject.Inject

class ActivitiesViewDataMapper @Inject constructor(
    private val wearIconMapper: WearIconMapper,
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
    ): ActivitiesListState.Content {
        val currentActivitiesMap = currentActivities.associateBy { it.id }
        val items = activities.map { activity ->
            mapItem(
                activity = activity,
                currentActivity = currentActivitiesMap[activity.id],
            )
        }

        return ActivitiesListState.Content(
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
            ?.let { " - $it" }
            .orEmpty()
        val icon = wearIconMapper.mapIcon(activity.icon)

        return ActivityChipState(
            id = activity.id,
            name = activity.name,
            icon = icon,
            color = activity.color,
            startedAt = currentActivity?.startedAt,
            tagString = tagString,
        )
    }
}