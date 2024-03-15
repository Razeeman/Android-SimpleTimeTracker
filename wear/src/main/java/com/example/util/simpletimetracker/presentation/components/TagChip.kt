/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Checkbox
import androidx.wear.compose.material.CheckboxDefaults
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.SplitToggleChip
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChipDefaults
import androidx.wear.tooling.preview.devices.WearDevices

@Immutable
data class TagChipState(
    val id: Long,
    val name: String,
    val color: Long,
    val checked: Boolean,
    val mode: TagSelectionMode,
) {
    enum class TagSelectionMode {
        SINGLE,
        MULTI,
    }
}

@Composable
fun TagChip(
    state: TagChipState,
    onClick: (Long) -> Unit = {},
) {
    when (state.mode) {
        TagChipState.TagSelectionMode.SINGLE -> {
            SingleSelectTagChip(
                state = state,
                onClick = onClick,
            )
        }

        TagChipState.TagSelectionMode.MULTI -> {
            MultiSelectTagChip(
                state = state,
                onClick = onClick,
            )
        }
    }
}

@Composable
private fun SingleSelectTagChip(
    state: TagChipState,
    onClick: (Long) -> Unit,
) {
    val onClickState = remember(state.id) {
        { onClick(state.id) }
    }
    Chip(
        modifier = Modifier
            .height(ACTIVITY_VIEW_HEIGHT.dp)
            .fillMaxWidth(),
        onClick = onClickState,
        label = {
            Text(
                text = state.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        colors = ChipDefaults.chipColors(
            backgroundColor = Color(state.color),
        ),
    )
}

@Composable
private fun MultiSelectTagChip(
    state: TagChipState,
    onClick: (Long) -> Unit = {},
) {
    val onCheckedChange: (Boolean) -> Unit = remember(state.id) {
        { onClick(state.id) }
    }
    val onClickState = remember(state.id) {
        { onClick(state.id) }
    }
    SplitToggleChip(
        modifier = Modifier
            .height(ACTIVITY_VIEW_HEIGHT.dp)
            .fillMaxWidth(),
        checked = state.checked,
        onCheckedChange = onCheckedChange,
        onClick = onClickState,
        label = {
            Text(
                text = state.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        toggleControl = {
            Checkbox(
                checked = state.checked,
                colors = CheckboxDefaults.colors(
                    checkedBoxColor = Color.White,
                    checkedCheckmarkColor = Color.White,
                    uncheckedBoxColor = Color.White,
                    uncheckedCheckmarkColor = Color.White,
                ),
            )
        },
        colors = ToggleChipDefaults.splitToggleChipColors(
            backgroundColor = Color(state.color),
            splitBackgroundOverlayColor = if (state.checked) {
                Color.White.copy(alpha = .1F)
            } else {
                Color.Black.copy(alpha = .3F)
            },
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun Default() {
    TagChip(
        state = TagChipState(
            id = 123,
            name = "Sleep",
            color = 0xFF123456,
            checked = false,
            mode = TagChipState.TagSelectionMode.SINGLE,
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun MultiSelectMode() {
    TagChip(
        state = TagChipState(
            id = 123,
            name = "Sleep",
            color = 0xFF654321,
            checked = false,
            mode = TagChipState.TagSelectionMode.MULTI,
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun MultiSelectChecked() {
    MultiSelectTagChip(
        state = TagChipState(
            id = 123,
            name = "Sleep",
            color = 0xFF654321,
            checked = true,
            mode = TagChipState.TagSelectionMode.MULTI,
        ),
    )
}