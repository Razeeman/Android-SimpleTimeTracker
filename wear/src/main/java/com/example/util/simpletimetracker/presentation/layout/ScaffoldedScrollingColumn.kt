/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.layout

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.ScalingLazyListScope
import androidx.wear.compose.material.rememberScalingLazyListState

@Composable
fun ScaffoldedScrollingColumn(content: ScalingLazyListScope.() -> Unit) {
    val scrollState = rememberScalingLazyListState()
    Scaffolding(scrollState) {
        ScrollingColumn(scrollState, content)
    }
}