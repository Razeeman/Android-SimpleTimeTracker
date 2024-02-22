/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.example.util.simpletimetracker.presentation.components.TagList
import com.example.util.simpletimetracker.presentation.mediators.CurrentActivitiesMediator
import com.example.util.simpletimetracker.presentation.remember.rememberCurrentActivities
import com.example.util.simpletimetracker.presentation.remember.rememberRPCClient
import com.example.util.simpletimetracker.presentation.remember.rememberTags
import com.example.util.simpletimetracker.wearrpc.CurrentActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant

@Composable
fun TagsScreen(activityId: Long, onSelectTag: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val rpc = rememberRPCClient()
    val (tags) = rememberTags(activityId)
    val (currentActivities) = rememberCurrentActivities()
    val currentActivitiesMediator = CurrentActivitiesMediator(rpc, currentActivities)

    TagList(
        tags,
        onSelectTag = {
            coroutineScope.launch(Dispatchers.Default) {
                currentActivitiesMediator.start(activityId, arrayOf(it))
                coroutineScope.launch(Dispatchers.Main) {
                    onSelectTag()
                }
            }
        },
    )
}
