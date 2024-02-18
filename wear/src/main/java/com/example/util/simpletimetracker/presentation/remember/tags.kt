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
import com.example.util.simpletimetracker.wearrpc.Tag
import com.example.util.simpletimetracker.wearrpc.WearRPCClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

/**
 * Handles asynchronous retrieval of the available tags for a specific activity in
 * Simple Time Tracker on the connected phone.
 *
 * Usage:
 * ```
 * val activityId: Long = /* obtain the ID (e.g. as a parameter) */
 * val rpc = /* create a WearRPCClient instance */
 * val (tags, refresh) = rememberTags(rpc, activityId)
 * ```
 *
 * Initially, `tags` will be an empty array. Once the actual tags are received
 * from the phone, `tags` will *automatically* update to that array and the encapsulating
 * Composable will *automatically* re-render.
 *
 * `refresh` is a function you can call to forcibly re-request the array of available tags
 * from the phone.
 */
@Composable
fun rememberTags(rpc: WearRPCClient, activityId: Long): Pair<Array<Tag>, () -> Unit> {
    var tags: Array<Tag> by remember { mutableStateOf(arrayOf()) }
    var tagsQueryCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(
        key1 = tagsQueryCount,
        block = {
            async(Dispatchers.Default) {
                tags = rpc.queryTagsForActivity(activityId)
            }
        },
    )
    return Pair(tags) { tagsQueryCount++ }
}