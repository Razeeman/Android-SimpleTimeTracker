/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.screens

import androidx.compose.runtime.Composable
import com.example.util.simpletimetracker.presentation.components.ActivitiesList
import com.example.util.simpletimetracker.presentation.remember.rememberActivities
import com.example.util.simpletimetracker.presentation.remember.rememberCurrentActivities
import com.example.util.simpletimetracker.wearrpc.Activity

@Composable
fun ActivitiesScreen(onSelectActivity: (activityId: Long) -> Unit) {
    val (activities, refreshActivities) = rememberActivities()
    val (currentActivities, setCurrentActivities, refreshCurrentActivities) = rememberCurrentActivities()

    ActivitiesList(
        activities,
        currentActivities,
        onSelectActivity = {
            onSelectActivity(it.id)
        },
        onDeselectActivity = { deselectedActivity: Activity ->
            val remainingActivities =
                currentActivities.filter { it.id != deselectedActivity.id }.toTypedArray()
            setCurrentActivities(remainingActivities)
        },
        onRefresh = {
            refreshActivities()
            refreshCurrentActivities()
        },
    )
}
