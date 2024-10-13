/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.ui.layout

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyListScope
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState

@Composable
fun ScaffoldedScrollingColumn(
    startItemIndex: Int,
    spacedBy: Dp = 10.dp,
    content: ScalingLazyListScope.() -> Unit,
) {
    val scrollState = rememberScalingLazyListState()
    Scaffolding(scrollState) {
        ScrollingColumn(
            startItemIndex = startItemIndex,
            spacedBy = spacedBy,
            scrollState = scrollState,
            content = content,
        )
    }
}