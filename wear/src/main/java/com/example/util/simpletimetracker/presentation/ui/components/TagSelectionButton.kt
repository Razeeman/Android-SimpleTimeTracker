/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.util.simpletimetracker.presentation.theme.ColorActive
import com.example.util.simpletimetracker.presentation.theme.ColorInactive

@Composable
fun TagSelectionButton(
    text: String,
    color: Color,
    onClick: () -> Unit = {},
) {
    Chip(
        modifier = Modifier
            .height(ACTIVITY_VIEW_HEIGHT.dp)
            .fillMaxWidth(),
        onClick = onClick,
        label = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = text,
                maxLines = 1,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
            )
        },
        colors = ChipDefaults.chipColors(
            backgroundColor = color,
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun Untagged() {
    TagSelectionButton(
        text = "Untagged",
        color = ColorInactive,
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun Save() {
    TagSelectionButton(
        text = "Save",
        color = ColorActive,
    )
}
