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
import com.example.util.simpletimetracker.wearrpc.Tag

@Composable
fun TagList(
    tags: Array<Tag>,
    mode: TagSelectionMode = TagSelectionMode.SINGLE,
    onSelectionComplete: (tags: Array<Tag>) -> Unit = {},
) {
    var selectedTags: List<Tag> by remember { mutableStateOf(listOf()) }
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
                            when (mode) {
                                TagSelectionMode.SINGLE -> {
                                    onSelectionComplete(arrayOf(tag))
                                }

                                TagSelectionMode.MULTI -> {
                                    if (selectedTags.contains(tag)) {
                                        selectedTags = selectedTags.minus(tag)
                                    } else {
                                        selectedTags = selectedTags.plus(tag)
                                    }
                                }
                            }
                        },
                    )
                }
            }
        }
        item { SubmitButton(onClick = { onSelectionComplete(selectedTags.toTypedArray()) }) }
    }
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun NoTags() {
    TagList(tags = arrayOf(), onSelectionComplete = {})
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun WithSomeTags() {
    TagList(
        tags = arrayOf(
            Tag(id = 123, name = "Sleep", isGeneral = false),
            Tag(id = 124, name = "Personal", isGeneral = true),
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun MultiSelectMode() {
    TagList(
        tags = arrayOf(
            Tag(id = 123, name = "Sleep", isGeneral = false),
            Tag(id = 124, name = "Personal", isGeneral = true),
        ),
        mode = TagSelectionMode.MULTI,
    )
}