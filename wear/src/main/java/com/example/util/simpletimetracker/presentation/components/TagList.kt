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
    mode: TagSelectionMode = TagSelectionMode.SINGLE,
    onSelectionComplete: (tags: List<WearTag>) -> Unit = {},
) {
    var selectedTags: List<WearTag> by remember { mutableStateOf(listOf()) }
    ScaffoldedScrollingColumn {
        if (tags.isEmpty()) {
            item {
                Text(
                    LocalContext.current.getString(R.string.no_tags),
                    modifier = Modifier.padding(8.dp),
                )
            }
        } else {
            for (tag in tags) {
                item {
                    TagChip(
                        tag = tag, mode = mode,
                        onClick = {
                            // No duplicate tags
                            onSelectionComplete(selectedTags.minus(tag).plus(tag))
                        },
                        onToggleOn = { selectedTags = selectedTags.plus(tag) },
                        onToggleOff = { selectedTags = selectedTags.minus(tag) },
                    )
                }
            }
        }
        item { SubmitButton(onClick = { onSelectionComplete(selectedTags) }) }
    }
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun NoTags() {
    TagList(tags = emptyList(), onSelectionComplete = {})
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun WithSomeTags() {
    TagList(
        tags = listOf(
            WearTag(id = 123, name = "Sleep", isGeneral = false, color = 0xFF123456),
            WearTag(id = 124, name = "Personal", isGeneral = true, color = 0xFF123456),
        ),
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
        mode = TagSelectionMode.MULTI,
    )
}