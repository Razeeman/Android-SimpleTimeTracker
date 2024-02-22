/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.example.util.simpletimetracker.presentation.components.ActivitiesList
import com.example.util.simpletimetracker.presentation.mediators.CurrentActivitiesMediator
import com.example.util.simpletimetracker.presentation.remember.rememberActivities
import com.example.util.simpletimetracker.presentation.remember.rememberCurrentActivities
import com.example.util.simpletimetracker.presentation.remember.rememberRPCClient
import com.example.util.simpletimetracker.wearrpc.Activity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ActivitiesScreen(onSelectActivity: (activityId: Long) -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val rpc = rememberRPCClient()
    val (activities, refreshActivities) = rememberActivities()
    val (currentActivities, refreshCurrentActivities) = rememberCurrentActivities()
    val currentActivitiesMediator = CurrentActivitiesMediator(rpc, currentActivities)
    val refresh = {
        refreshActivities()
        refreshCurrentActivities()
    }

    ActivitiesList(
        activities,
        currentActivities,
        onSelectActivity = {
            coroutineScope.launch(Dispatchers.Default) {
                val activityTags = rpc.queryTagsForActivity(it.id)
                if (activityTags.isNotEmpty()) {
                    coroutineScope.launch(Dispatchers.Main) { onSelectActivity(it.id) }
                } else {
                    currentActivitiesMediator.start(it.id)
                    refresh()
                }
            }
        },
        onEnableActivity = {
            coroutineScope.launch(Dispatchers.Default) {
                currentActivitiesMediator.start(it.id)
                refresh()
            }
        },
        onDisableActivity = { deselectedActivity: Activity ->
            coroutineScope.launch(Dispatchers.Default) {
                currentActivitiesMediator.stop(deselectedActivity.id)
                refresh()
            }
        },
        onRefresh = refresh,
    )
}
