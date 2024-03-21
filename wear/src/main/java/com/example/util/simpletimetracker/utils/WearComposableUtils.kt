/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.utils

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.util.simpletimetracker.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import java.time.Duration

@SuppressLint("ComposableNaming")
@Composable
fun <T> Flow<T>.collectEffects(
    key: Any?,
    action: (T) -> Unit,
) {
    LaunchedEffect(key) {
        onEach { action(it) }.collect()
    }
}

@Composable
fun getString(@StringRes stringResId: Int): String {
    return LocalContext.current.getString(stringResId)
}

@Composable
fun OnLifecycle(
    onStart: () -> Unit = {},
    onResume: () -> Unit = {},
    onPause: () -> Unit = {},
    onStop: () -> Unit = {},
) {
    OnLifecycleEvent { event ->
        if (event == Lifecycle.Event.ON_START) onStart()
        if (event == Lifecycle.Event.ON_RESUME) onResume()
        if (event == Lifecycle.Event.ON_PAUSE) onPause()
        if (event == Lifecycle.Event.ON_STOP) onStop()
    }
}

@Composable
fun OnLifecycleEvent(onEvent: (Lifecycle.Event) -> Unit) {
    val eventHandler by rememberUpdatedState(onEvent)
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycle = lifecycleOwner.lifecycle

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            eventHandler(event)
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
}

// Copy from TimeMapper.formatInterval
@Composable
fun durationToLabel(duration: Duration): String {
    val hourString = getString(R.string.time_hour)
    val minuteString = getString(R.string.time_minute)
    val secondString = getString(R.string.time_second)

    val hr = duration.toHours()
    val min = duration.toMinutes() % 60
    val sec = duration.seconds % 60

    val willShowHours = hr != 0L
    val willShowMinutes = willShowHours || min != 0L
    val willShowSeconds = true

    var res = ""
    if (willShowHours) res += "$hr$hourString "
    if (willShowMinutes) res += "$min$minuteString"
    if (willShowMinutes && willShowSeconds) res += " "
    if (willShowSeconds) res += "$sec$secondString"

    return res
}

@Composable
fun durationToLabelShort(duration: Duration): String {
    val hr = duration.toHours()
    val min = duration.toMinutes() % 60
    val sec = duration.seconds % 60

    val willShowHours = hr != 0L
    val willShowMinutes = true
    val willShowSeconds = true

    var res = ""
    if (willShowHours) res += "${hr.toString().padDuration()}:"
    if (willShowMinutes) res += "${min.toString().padDuration()}:"
    if (willShowSeconds) res += sec.toString().padDuration()

    return res
}
