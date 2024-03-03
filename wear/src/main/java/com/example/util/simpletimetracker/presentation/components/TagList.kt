/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.ScalingLazyListScope
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.presentation.layout.ScaffoldedScrollingColumn
import com.example.util.simpletimetracker.presentation.utils.getString
import com.example.util.simpletimetracker.wear_api.WearTag

sealed interface TagListState {

    object Loading : TagListState

    data class Empty(
        @StringRes val messageResId: Int,
    ) : TagListState

    data class Content(
        val items: List<Item>,
        val mode: TagSelectionMode,
    ) : TagListState

    sealed interface Item {
        data class Tag(
            val tag: WearTag,
            val selected: Boolean,
        ) : Item

        data class Button(
            @StringRes val textResId: Int,
            val color: Color,
            val buttonType: ButtonType,
        ) : Item

        sealed interface ButtonType {
            object Complete : ButtonType
            object Untagged : ButtonType
        }
    }
}

@Composable
fun TagList(
    state: TagListState,
    onButtonClick: (TagListState.Item.ButtonType) -> Unit = {},
    onToggleClick: (WearTag) -> Unit = {},
) {
    ScaffoldedScrollingColumn {
        when (state) {
            is TagListState.Loading -> renderLoadingState()
            is TagListState.Empty -> renderEmptyState(
                state = state,
            )
            is TagListState.Content -> renderContentState(
                state = state,
                onButtonClick = onButtonClick,
                onToggleClick = onToggleClick,
            )
        }
    }
}

private fun renderLoadingState() {
    // Show nothing until data is loded.
}

private fun ScalingLazyListScope.renderEmptyState(
    state: TagListState.Empty,
) {
    item {
        Text(
            text = getString(state.messageResId),
            modifier = Modifier.padding(8.dp),
        )
    }
}

private fun ScalingLazyListScope.renderContentState(
    state: TagListState.Content,
    onButtonClick: (TagListState.Item.ButtonType) -> Unit = {},
    onToggleClick: (WearTag) -> Unit = {},
) {
    for (item in state.items) {
        when (item) {
            is TagListState.Item.Tag -> item {
                TagChip(
                    tag = item.tag,
                    mode = state.mode,
                    onClick = onToggleClick,
                    checked = item.selected,
                )
            }
            is TagListState.Item.Button -> item {
                TagSelectionButton(
                    text = getString(item.textResId),
                    color = item.color,
                    onClick = {
                        onButtonClick(item.buttonType)
                    },
                )
            }
        }
    }
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun NoTags() {
    TagList(
        state = TagListState.Empty(R.string.no_tags),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun WithSomeTags() {
    TagList(
        state = TagListState.Content(
            items = listOf(
                TagListState.Item.Tag(
                    tag = WearTag(id = 123, name = "Sleep", isGeneral = false, color = 0xFF123456),
                    selected = false,
                ),
                TagListState.Item.Tag(
                    tag = WearTag(id = 124, name = "Personal", isGeneral = true, color = 0xFF123456),
                    selected = false,
                ),
            ),
            mode = TagSelectionMode.SINGLE,
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun MultiSelectMode() {
    TagList(
        state = TagListState.Content(
            items = listOf(
                TagListState.Item.Tag(
                    tag = WearTag(id = 123, name = "Sleep", isGeneral = false, color = 0xFF123456),
                    selected = true,
                ),
                TagListState.Item.Tag(
                    tag = WearTag(id = 124, name = "Personal", isGeneral = true, color = 0xFF123456),
                    selected = false,
                ),
            ),
            mode = TagSelectionMode.MULTI,
        ),
    )
}