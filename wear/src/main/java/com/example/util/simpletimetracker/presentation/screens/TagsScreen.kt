/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.screens

import androidx.compose.runtime.Composable
import com.example.util.simpletimetracker.presentation.components.TagList
import com.example.util.simpletimetracker.presentation.remember.rememberCurrentActivities
import com.example.util.simpletimetracker.presentation.remember.rememberTags
import com.example.util.simpletimetracker.wearrpc.CurrentActivity
import java.time.Instant

@Composable
fun TagsScreen(activityId: Long, onSelectTag: () -> Unit) {
    val (tags) = rememberTags(activityId)
    val (_, setCurrents) = rememberCurrentActivities()

    TagList(
        tags,
        onSelectTag = {
            setCurrents(
                arrayOf(
                    CurrentActivity(activityId, Instant.now().toEpochMilli(), arrayOf(it)),
                ),
            )
            onSelectTag()
        },
    )
}
