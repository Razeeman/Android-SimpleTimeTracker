/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.example.util.simpletimetracker.presentation.components.TagList
import com.example.util.simpletimetracker.presentation.components.TagSelectionMode
import com.example.util.simpletimetracker.presentation.mediators.CurrentActivitiesMediator
import com.example.util.simpletimetracker.presentation.remember.rememberCurrentActivities
import com.example.util.simpletimetracker.presentation.remember.rememberRPCClient
import com.example.util.simpletimetracker.presentation.remember.rememberSettings
import com.example.util.simpletimetracker.presentation.remember.rememberTags
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun TagsScreen(activityId: Long, onComplete: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val rpc = rememberRPCClient()
    val (settings) = rememberSettings()
    val (tags) = rememberTags(activityId)
    val (currentActivities) = rememberCurrentActivities()
    val currentActivitiesMediator = CurrentActivitiesMediator(rpc, currentActivities)

    TagList(
        tags,
        mode = if (settings?.recordTagSelectionCloseAfterOne != false) {
            TagSelectionMode.SINGLE
        } else {
            TagSelectionMode.MULTI
        },
        onSelectionComplete = {
            coroutineScope.launch(Dispatchers.Default) {
                currentActivitiesMediator.start(activityId, it)
                coroutineScope.launch(Dispatchers.Main) { onComplete() }
            }
        },
    )
}
