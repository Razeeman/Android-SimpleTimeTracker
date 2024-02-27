/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.screens

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import com.example.util.simpletimetracker.presentation.components.ActivitiesList
import com.example.util.simpletimetracker.presentation.mediators.CurrentActivitiesMediator
import com.example.util.simpletimetracker.presentation.remember.rememberActivities
import com.example.util.simpletimetracker.presentation.remember.rememberCurrentActivities
import com.example.util.simpletimetracker.presentation.remember.rememberRPCClient
import com.example.util.simpletimetracker.wearrpc.Activity
import com.example.util.simpletimetracker.wearrpc.StartActivityMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ActivitiesScreen(onRequestTagSelection: (activityId: Long) -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val rpc = rememberRPCClient()
    val (activities, refreshActivities) = rememberActivities()
    val (currentActivities, refreshCurrentActivities) = rememberCurrentActivities()
    val refresh = {
        refreshActivities()
        refreshCurrentActivities()
    }
    val currentActivitiesMediator = CurrentActivitiesMediator(rpc, currentActivities)
    val startActivityWithoutTags: (Activity) -> Unit = {
        Log.d("ActivitiesScreen", "Starting ${it.name} (#${it.id}) without tags")
        coroutineScope.launch(Dispatchers.Default) {
            currentActivitiesMediator.start(it.id)
            refresh()
        }
    }
    val startActivityWithTags: (Activity) -> Unit = {
        coroutineScope.launch(Dispatchers.Main) {
            Log.d("ActivitiesScreen", "Starting ${it.name} (#${it.id}) with tags")
            onRequestTagSelection(it.id)
        }
    }
    val stopActivity: (Activity) -> Unit = {
        Log.d("ActivitiesScreen", "Stopping ${it.name} (#${it.id})")
        coroutineScope.launch(Dispatchers.Default) {
            currentActivitiesMediator.stop(it.id)
            refresh()
        }
    }

    val startActivitiesMediator = StartActivityMediator(
        rpc,
        onRequestStartActivity = startActivityWithoutTags,
        onRequestTagSelection = startActivityWithTags,
    )


    ActivitiesList(
        activities = activities,
        currentActivities = currentActivities,
        onSelectActivity = {
            coroutineScope.launch(Dispatchers.Default) {
                startActivitiesMediator.requestStart(it)
            }
        },
        onEnableActivity = startActivityWithoutTags,
        onDisableActivity = stopActivity,
        onRefresh = refresh,
    )
}
