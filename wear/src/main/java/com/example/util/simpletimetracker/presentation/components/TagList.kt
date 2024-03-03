/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.presentation.layout.ScaffoldedScrollingColumn
import com.example.util.simpletimetracker.wear_api.WearTag

@Composable
fun TagList(
    tags: List<WearTag>,
    selectedTags: List<WearTag>,
    mode: TagSelectionMode,
    onSelectionComplete: () -> Unit = {},
    onToggleClick: (WearTag) -> Unit = {},
) {
    ScaffoldedScrollingColumn {
        if (tags.isEmpty()) {
            item {
                Text(
                    text = LocalContext.current.getString(R.string.no_tags),
                    modifier = Modifier.padding(8.dp),
                )
            }
        } else {
            for (tag in tags) {
                item {
                    TagChip(
                        tag = tag,
                        mode = mode,
                        onClick = onSelectionComplete,
                        onToggleClick = onToggleClick,
                        checked = tag.id in selectedTags.map { it.id },
                    )
                }
            }
        }
        item {
            SubmitButton(
                onClick = onSelectionComplete,
            )
        }
    }
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun NoTags() {
    TagList(
        tags = emptyList(),
        selectedTags = emptyList(),
        mode = TagSelectionMode.SINGLE,
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun WithSomeTags() {
    TagList(
        tags = listOf(
            WearTag(id = 123, name = "Sleep", isGeneral = false, color = 0xFF123456),
            WearTag(id = 124, name = "Personal", isGeneral = true, color = 0xFF123456),
        ),
        selectedTags = emptyList(),
        mode = TagSelectionMode.SINGLE,
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun MultiSelectMode() {
    TagList(
        tags = listOf(
            WearTag(id = 123, name = "Sleep", isGeneral = false, color = 0xFF123456),
            WearTag(id = 124, name = "Personal", isGeneral = true, color = 0xFF123456),
        ),
        selectedTags = listOf(
            WearTag(id = 123, name = "Sleep", isGeneral = false, color = 0xFF123456),
        ),
        mode = TagSelectionMode.MULTI,
    )
}