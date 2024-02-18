/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.ScalingLazyListScope
import androidx.wear.compose.material.ScalingLazyListState
import androidx.wear.compose.material.rememberScalingLazyListState
import com.google.android.horologist.compose.focus.rememberActiveFocusRequester
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import com.google.android.horologist.compose.rotaryinput.rememberRotaryHapticFeedback
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll

@OptIn(ExperimentalHorologistComposeLayoutApi::class)
@Composable
fun ScrollingColumn(
    scrollState: ScalingLazyListState = rememberScalingLazyListState(),
    content: ScalingLazyListScope.() -> Unit,
) {
    val focusRequester = rememberActiveFocusRequester()
    val rotaryHapticFeedback = rememberRotaryHapticFeedback()
    ScalingLazyColumn(
        modifier = Modifier
            .rotaryWithScroll(focusRequester, scrollState, rotaryHapticFeedback)
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .selectableGroup(),
        contentPadding = PaddingValues(10.dp),
        verticalArrangement = Arrangement.Center,
        state = scrollState,
        content = content,
    )
}