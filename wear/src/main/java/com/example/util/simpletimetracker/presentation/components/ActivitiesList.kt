/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.util.simpletimetracker.presentation.layout.ScaffoldedScrollingColumn
import com.example.util.simpletimetracker.wearrpc.Activity
import com.example.util.simpletimetracker.wearrpc.CurrentActivity

@Composable
fun ActivitiesList(
    activities: Array<Activity>,
    currentActivities: Array<CurrentActivity>,
    onSelectActivity: (activity: Activity) -> Unit,
    onEnableActivity: (activity: Activity) -> Unit,
    onDisableActivity: (activity: Activity) -> Unit,
    onRefresh: () -> Unit,
) {
    ScaffoldedScrollingColumn {
        for (activity in activities) {
            val currentActivity = currentActivities.filter { it.id == activity.id }.getOrNull(0)
            item(key = activity.id) {
                ActivityChip(
                    activity,
                    startedAt = currentActivity?.startedAt,
                    tags = currentActivity?.tags ?: arrayOf(),
                    onPress = { onSelectActivity(activity) },
                    onToggleOn = { onEnableActivity(activity) },
                    onToggleOff = { onDisableActivity(activity) },
                )
            }
        }
        item { RefreshButton(onClick = onRefresh) }
    }
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun Preview() {
    val activities = arrayOf(
        Activity(1234, "Chores", "🧹", 0xFFFA0000),
        Activity(4321, "Sleep", "🛏️", 0xFF0000FA),
    )
    val currents = arrayOf(
        CurrentActivity(id = 4321, startedAt = 1708241427000L, tags = arrayOf()),
    )
    ActivitiesList(
        activities,
        currentActivities = currents,
        onSelectActivity = { /* `it` is the selected activity */ },
        onEnableActivity = { /* `it` is the enabled activity */ },
        onDisableActivity = { /* `it` is the disabled activity */ },
        onRefresh = { /* What to do when requesting a refresh */ },
    )
}