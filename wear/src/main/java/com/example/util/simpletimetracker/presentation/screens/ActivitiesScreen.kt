/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.util.simpletimetracker.presentation.components.ActivitiesList
import com.example.util.simpletimetracker.presentation.remember.rememberActivities
import com.example.util.simpletimetracker.presentation.remember.rememberCurrentActivities
import com.example.util.simpletimetracker.wearrpc.ContextMessenger
import com.example.util.simpletimetracker.wearrpc.WearRPCClient


@Composable
fun ActivitiesScreen(onSelectActivity: (activityId: Long) -> Unit) {
    val rpc = WearRPCClient(ContextMessenger(LocalContext.current))
    val (activities, refreshActivities) = rememberActivities(rpc)
    val (currentActivities, refreshCurrentActivities) = rememberCurrentActivities(rpc)

    ActivitiesList(
        activities,
        currentActivities,
        onSelectActivity = {
            onSelectActivity(it.id)
        },
        onRefresh = {
            refreshActivities()
            refreshCurrentActivities()
        },
    )
}



