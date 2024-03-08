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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

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
