/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.ui.components

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.util.simpletimetracker.presentation.theme.ColorActive
import com.example.util.simpletimetracker.presentation.theme.ColorInactive

@Immutable
data class TagSelectionButtonState(
    val text: String,
    val color: Color,
    val buttonType: TagListState.Item.ButtonType,
    val isLoading: Boolean = false,
)

@Composable
fun TagSelectionButton(
    state: TagSelectionButtonState,
    onClick: (TagListState.Item.ButtonType) -> Unit = {},
) {
    val onClickState = remember(state) {
        { onClick(state.buttonType) }
    }
    Chip(
        modifier = Modifier
            .height(ACTIVITY_VIEW_HEIGHT.dp)
            .fillMaxWidth(),
        onClick = onClickState,
        label = {
            if (!state.isLoading) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = state.text,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                )
            } else {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.7f),
                )
            }
        },
        colors = ChipDefaults.chipColors(
            backgroundColor = state.color,
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun Untagged() {
    TagSelectionButton(
        state = TagSelectionButtonState(
            text = "Untagged",
            color = ColorInactive,
            buttonType = TagListState.Item.ButtonType.Untagged,
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun Save() {
    TagSelectionButton(
        state = TagSelectionButtonState(
            text = "Save",
            color = ColorActive,
            buttonType = TagListState.Item.ButtonType.Complete,
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun Loading() {
    TagSelectionButton(
        state = TagSelectionButtonState(
            text = "Loading",
            color = ColorActive,
            buttonType = TagListState.Item.ButtonType.Complete,
            isLoading = true,
        ),
    )
}
