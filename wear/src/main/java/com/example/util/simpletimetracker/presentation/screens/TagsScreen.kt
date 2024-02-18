/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.util.simpletimetracker.presentation.components.TagList
import com.example.util.simpletimetracker.presentation.remember.rememberTags
import com.example.util.simpletimetracker.wearrpc.ContextMessenger
import com.example.util.simpletimetracker.wearrpc.WearRPCClient

@Composable
fun TagsScreen(activityId: Long, onSelectTag: () -> Unit) {
    val rpc = WearRPCClient(ContextMessenger(LocalContext.current))
    val (tags) = rememberTags(rpc, activityId)

    TagList(tags, onSelectTag = { onSelectTag() } )
}