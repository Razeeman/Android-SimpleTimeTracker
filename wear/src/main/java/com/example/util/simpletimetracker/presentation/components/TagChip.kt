/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Checkbox
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.util.simpletimetracker.wearrpc.Tag

enum class TagSelectionMode {
    SINGLE, MULTI,
}

@Composable
fun TagChip(tag: Tag, onClick: () -> Unit, mode: TagSelectionMode = TagSelectionMode.SINGLE) {
    when (mode) {
        TagSelectionMode.SINGLE -> {
            SingleSelectTagChip(tag = tag, onClick = onClick)
        }

        TagSelectionMode.MULTI -> {
            MultiSelectTagChip(tag = tag, onClick = onClick)
        }
    }

}

@Composable
private fun SingleSelectTagChip(tag: Tag, onClick: () -> Unit) {
    Chip(
        onClick = onClick,
        label = { Text(tag.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(top = 10.dp),
    )
}

@Composable
private fun MultiSelectTagChip(tag: Tag, onClick: () -> Unit) {
    var checked by remember { mutableStateOf(false) }
    ToggleChip(
        checked = checked,
        onCheckedChange = {
            checked = !checked
            onClick()
        },
        label = { Text(tag.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        toggleControl = { Checkbox(checked = checked) },
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(top = 10.dp)
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun Default() {
    TagChip(tag = Tag(id = 123, name = "Sleep", isGeneral = false), onClick = {})
}


@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun MultiSelectMode() {
    TagChip(
        tag = Tag(id = 123, name = "Sleep", isGeneral = false),
        onClick = {},
        mode = TagSelectionMode.MULTI,
    )
}