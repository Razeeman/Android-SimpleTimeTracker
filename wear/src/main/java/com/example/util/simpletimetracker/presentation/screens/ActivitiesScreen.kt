/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.example.util.simpletimetracker.presentation.components.ActivitiesList
import com.example.util.simpletimetracker.presentation.remember.rememberActivities
import com.example.util.simpletimetracker.presentation.remember.rememberCurrentActivities
import com.example.util.simpletimetracker.presentation.remember.rememberRPCClient
import com.example.util.simpletimetracker.wearrpc.Activity
import com.example.util.simpletimetracker.wearrpc.CurrentActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ActivitiesScreen(onSelectActivity: (activityId: Long) -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val rpc = rememberRPCClient()
    val (activities, refreshActivities) = rememberActivities()
    val (currentActivities, setCurrentActivities, refreshCurrentActivities) = rememberCurrentActivities()
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
                    rpc.setCurrentActivities(
                        arrayOf(
                            CurrentActivity(
                                it.id,
                                System.currentTimeMillis(),
                                arrayOf(),
                            ),
                        ),
                    )
                    refresh()
                }
            }
        },
        onEnableActivity = {
            coroutineScope.launch(Dispatchers.Default) {
                rpc.setCurrentActivities(
                    arrayOf(
                        CurrentActivity(
                            it.id,
                            System.currentTimeMillis(),
                            arrayOf(),
                        ),
                    ),
                )
                refresh()
            }
        },
        onDisableActivity = { deselectedActivity: Activity ->
            val remainingActivities =
                currentActivities.filter { it.id != deselectedActivity.id }.toTypedArray()
            setCurrentActivities(remainingActivities)
            refresh()
        },
        onRefresh = refresh,
    )
}
