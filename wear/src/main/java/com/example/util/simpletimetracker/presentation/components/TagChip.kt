/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.Checkbox
import androidx.wear.compose.material.CheckboxDefaults
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.SplitToggleChip
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChipDefaults
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.util.simpletimetracker.wear_api.WearTag

enum class TagSelectionMode {
    SINGLE,
    MULTI,
}

@Composable
fun TagChip(
    tag: WearTag,
    onClick: (WearTag) -> Unit = {},
    mode: TagSelectionMode = TagSelectionMode.SINGLE,
    checked: Boolean,
) {
    when (mode) {
        TagSelectionMode.SINGLE -> {
            SingleSelectTagChip(
                tag = tag,
                onClick = onClick,
            )
        }

        TagSelectionMode.MULTI -> {
            MultiSelectTagChip(
                tag = tag,
                onClick = onClick,
                checked = checked,
            )
        }
    }
}

@Composable
private fun SingleSelectTagChip(
    tag: WearTag,
    onClick: (WearTag) -> Unit,
) {
    Chip(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            onClick(tag)
        },
        label = {
            Text(
                text = tag.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        colors = ChipDefaults.chipColors(
            backgroundColor = Color(tag.color),
        ),
    )
}

@Composable
private fun MultiSelectTagChip(
    tag: WearTag,
    onClick: (WearTag) -> Unit = {},
    checked: Boolean,
) {
    SplitToggleChip(
        checked = checked,
        onCheckedChange = {
            onClick(tag)
        },
        onClick = {
            onClick(tag)
        },
        label = {
            Text(
                text = tag.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        toggleControl = {
            Checkbox(
                checked = checked,
                colors = CheckboxDefaults.colors(
                    checkedBoxColor = Color.White,
                    checkedCheckmarkColor = Color.White,
                    uncheckedBoxColor = Color.White,
                    uncheckedCheckmarkColor = Color.White,
                ),
            )
        },
        colors = ToggleChipDefaults.splitToggleChipColors(
            backgroundColor = Color(tag.color),
            splitBackgroundOverlayColor = if (checked) {
                Color.White.copy(alpha = .1F)
            } else {
                Color.Black.copy(alpha = .3F)
            },
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun Default() {
    TagChip(
        tag = WearTag(id = 123, name = "Sleep", isGeneral = false, color = 0xFF123456),
        onClick = {},
        checked = false,
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun MultiSelectMode() {
    TagChip(
        tag = WearTag(id = 123, name = "Sleep", isGeneral = false, color = 0xFF654321),
        onClick = {},
        mode = TagSelectionMode.MULTI,
        checked = false,
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun MultiSelectChecked() {
    MultiSelectTagChip(
        tag = WearTag(id = 123, name = "Sleep", isGeneral = false, color = 0xFF654321),
        onClick = {},
        checked = true,
    )
}