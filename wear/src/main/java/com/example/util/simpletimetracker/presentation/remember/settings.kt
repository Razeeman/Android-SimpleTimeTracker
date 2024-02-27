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
import com.example.util.simpletimetracker.wearrpc.Settings
import com.example.util.simpletimetracker.wearrpc.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

/**
 * Handles asynchronous retrieval of the settings for Simple Time Tracker on the connected phone.
 *
 * Usage:
 * ```
 * val (settings, refresh) = rememberSettings()
 * ```
 *
 * Initially, `settings` will be `null`. Once the actual settings are received
 * from the phone, `settings` will *automatically* update to the object and the encapsulating
 * Composable will *automatically* re-render.
 *
 * `refresh` is a function you can call to forcibly re-request the settings from the phone.
 */
@Composable
fun rememberSettings(): Pair<Settings?, () -> Unit> {
    var rpc = rememberRPCClient()
    var settings: Settings? by remember { mutableStateOf(null) }
    var settingsQueryCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(settingsQueryCount) {
        async(Dispatchers.Default) {
            settings = rpc.querySettings()
        }
    }
    return Pair(settings) { settingsQueryCount++ }
}