/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.remember

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.util.simpletimetracker.wearrpc.CurrentActivity
import com.example.util.simpletimetracker.wearrpc.WearRPCClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

/**
 * Handles asynchronous retrieval of the currently running activities in Simple Time Tracker on the
 * connected phone.
 *
 * Usage:
 * ```
 * val rpc = /* create a WearRPCClient instance */
 * val (currents, refresh) = rememberCurrentActivities(rpc)
 * ```
 *
 * Initially, `currents` will be an empty array. Once the actual current activities are received
 * from the phone, `currents` will *automatically* update to that array and the encapsulating
 * Composable will *automatically* re-render.
 *
 * `refresh` is a function you can call to forcibly re-request the array of current activities
 * from the phone.
 */
@Composable
fun rememberCurrentActivities(rpc: WearRPCClient): Pair<Array<CurrentActivity>, () -> Unit> {
    var currentActivities: Array<CurrentActivity> by remember { mutableStateOf(arrayOf()) }
    var currentActivitiesQueryCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(
        key1 = currentActivitiesQueryCount,
        block = {
            async(Dispatchers.Default) {
                currentActivities = rpc.queryCurrentActivities()
            }
        },
    )
    return Pair(currentActivities) { currentActivitiesQueryCount++ }
}