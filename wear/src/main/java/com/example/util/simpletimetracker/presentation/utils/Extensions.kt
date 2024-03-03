/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.utils

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
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