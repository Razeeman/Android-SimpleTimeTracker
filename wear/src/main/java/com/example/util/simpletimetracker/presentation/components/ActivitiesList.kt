/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.presentation.layout.ScaffoldedScrollingColumn
import com.example.util.simpletimetracker.wear_api.Activity
import com.example.util.simpletimetracker.wear_api.CurrentActivity

@Composable
fun ActivitiesList(
    activities: List<Activity>,
    currentActivities: List<CurrentActivity>,
    onSelectActivity: (activity: Activity) -> Unit,
    onEnableActivity: (activity: Activity) -> Unit,
    onDisableActivity: (activity: Activity) -> Unit,
    onRefresh: () -> Unit,
    footer: @Composable () -> Unit = {}
) {
    ScaffoldedScrollingColumn {
        if (activities.isEmpty()) {
            item {
                Text(
                    LocalContext.current.getString(R.string.no_activities),
                    modifier = Modifier.padding(8.dp),
                )
            }
        } else {
            for (activity in activities) {
                val currentActivity = currentActivities.filter { it.id == activity.id }.getOrNull(0)
                item(key = activity.id) {
                    ActivityChip(
                        activity,
                        startedAt = currentActivity?.startedAt,
                        tags = currentActivity?.tags.orEmpty(),
                        onClick = { onSelectActivity(activity) },
                        onToggleOn = { onEnableActivity(activity) },
                        onToggleOff = { onDisableActivity(activity) },
                    )
                }
            }
        }

        item { RefreshButton(onClick = onRefresh) }
        item { footer() }
    }
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun NoActivities() {
    ActivitiesList(
        activities = emptyList(),
        currentActivities = emptyList(),
        onSelectActivity = { /* `it` is the selected activity */ },
        onEnableActivity = { /* `it` is the enabled activity */ },
        onDisableActivity = { /* `it` is the disabled activity */ },
        onRefresh = { /* What to do when requesting a refresh */ },
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun Preview() {
    val activities = listOf(
        Activity(1234, "Chores", "🧹", 0xFFFA0000),
        Activity(4321, "Sleep", "🛏️", 0xFF0000FA),
    )
    val currents = listOf(
        CurrentActivity(id = 4321, startedAt = 1708241427000L, tags = emptyList()),
    )
    ActivitiesList(
        activities = activities,
        currentActivities = currents,
        onSelectActivity = { /* `it` is the selected activity */ },
        onEnableActivity = { /* `it` is the enabled activity */ },
        onDisableActivity = { /* `it` is the disabled activity */ },
        onRefresh = { /* What to do when requesting a refresh */ },
        footer = {
            Text("Sample Footer")
        }
    )
}