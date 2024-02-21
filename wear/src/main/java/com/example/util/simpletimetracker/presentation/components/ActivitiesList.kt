
/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.components

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.util.simpletimetracker.presentation.layout.ScaffoldedScrollingColumn
import com.example.util.simpletimetracker.presentation.remember.rememberRPCClient
import com.example.util.simpletimetracker.wearrpc.Activity
import com.example.util.simpletimetracker.wearrpc.CurrentActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ActivitiesList(
    activities: Array<Activity>,
    currentActivities: Array<CurrentActivity>,
    onSelectActivity: (activity: Activity) -> Unit,
    onDeselectActivity: (activity: Activity) -> Unit,
    onRefresh: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val rpcClient = rememberRPCClient()
    val context = LocalContext.current

    ScaffoldedScrollingColumn {
        for (activity in activities) {
            val currentActivity = currentActivities.filter { it.id == activity.id }.getOrNull(0)
            item {
                ActivityChip(
                    activity,
                    startedAt = currentActivity?.startedAt,
                    tags = currentActivity?.tags ?: arrayOf(),
                    onSelectActivity = {
                        coroutineScope.launch(Dispatchers.Default) {
                            val activityTags = rpcClient.queryTagsForActivity(activity.id)
                            coroutineScope.launch(Dispatchers.Main) {
                                if (activityTags.isNotEmpty()) {
                                    onSelectActivity(activity)
                                } else {
                                    Toast.makeText(context, "Activity has no tags", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    onSelectActivitySkipTagSelection = {
                        coroutineScope.launch(Dispatchers.Default) {
                            rpcClient.setCurrentActivities(
                                currentActivities.plus(
                                    CurrentActivity(
                                        activity.id,
                                        System.currentTimeMillis(),
                                        arrayOf(),
                                    ),
                                ),
                            )
                        }
                    },
                    onDeselectActivity = {
                        onDeselectActivity(activity)
                    },
                )
            }
        }
        item {
            RefreshButton(
                onClick = onRefresh,
                contentDescription = "Refresh Activities List",
            )
        }
    }
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun Preview() {
    val activities = arrayOf(
        Activity(1234, "Chores", "🧹", "#FA0000"),
        Activity(4321, "Sleep", "🛏️", "#0000FA"),
    )
    val currents = arrayOf(
        CurrentActivity(id = 4321, startedAt = 1708241427000L, tags = arrayOf()),
    )
    ActivitiesList(
        activities,
        currentActivities = currents,
        onSelectActivity = { /* `it` is the selected activity */ },
        onDeselectActivity = { /* `it` is the deselected activity */ },
        onRefresh = { /* What to do when requesting a refresh */ },
    )
}