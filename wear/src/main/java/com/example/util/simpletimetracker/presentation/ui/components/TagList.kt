/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyListScope
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.presentation.ui.layout.ScaffoldedScrollingColumn
import com.example.util.simpletimetracker.utils.getString

sealed interface TagListState {

    object Loading : TagListState

    data class Error(
        @StringRes val messageResId: Int,
    ) : TagListState

    data class Empty(
        @StringRes val messageResId: Int,
    ) : TagListState

    data class Content(
        val items: List<Item>,
    ) : TagListState

    sealed interface Item {
        data class Tag(
            val tag: TagChipState,
        ) : Item

        data class Button(
            val data: TagSelectionButtonState,
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
    onToggleClick: (Long) -> Unit = {},
    onRefresh: () -> Unit = {},
) {
    ScaffoldedScrollingColumn(
        startItemIndex = 0,
    ) {
        when (state) {
            is TagListState.Loading -> item {
                RenderLoadingState()
            }
            is TagListState.Error -> item {
                RenderErrorState(state, onRefresh)
            }
            is TagListState.Empty -> item {
                RenderEmptyState(state)
            }
            is TagListState.Content -> {
                renderContentState(
                    state = state,
                    onButtonClick = onButtonClick,
                    onToggleClick = onToggleClick,
                )
            }
        }
    }
}

@Composable
private fun RenderLoadingState() {
    CircularProgressIndicator(
        modifier = Modifier.width(64.dp),
    )
}

@Composable
private fun RenderErrorState(
    state: TagListState.Error,
    onRefresh: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(R.drawable.wear_connection_error),
            contentDescription = null,
        )
        Text(
            text = getString(stringResId = state.messageResId),
            modifier = Modifier.padding(8.dp),
            textAlign = TextAlign.Center,
        )
        RefreshButton(onRefresh)
    }
}

@Composable
private fun RenderEmptyState(
    state: TagListState.Empty,
) {
    Text(
        text = getString(state.messageResId),
        modifier = Modifier.padding(8.dp),
    )
}

private fun ScalingLazyListScope.renderContentState(
    state: TagListState.Content,
    onButtonClick: (TagListState.Item.ButtonType) -> Unit = {},
    onToggleClick: (Long) -> Unit = {},
) {
    for (itemState in state.items) {
        when (itemState) {
            is TagListState.Item.Tag -> item {
                TagChip(
                    state = itemState.tag,
                    onClick = onToggleClick,
                )
            }
            is TagListState.Item.Button -> item {
                TagSelectionButton(
                    state = itemState.data,
                    onClick = onButtonClick,
                )
            }
        }
    }
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun Loading() {
    TagList(
        state = TagListState.Loading,
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun Error() {
    TagList(
        state = TagListState.Error(R.string.wear_loading_error),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun NoTags() {
    TagList(
        state = TagListState.Empty(R.string.change_record_categories_empty),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun WithSomeTags() {
    TagList(
        state = TagListState.Content(
            items = listOf(
                TagListState.Item.Tag(
                    tag = TagChipState(
                        id = 123,
                        name = "Sleep",
                        color = 0xFF123456,
                        checked = false,
                        mode = TagChipState.TagSelectionMode.SINGLE,
                    ),
                ),
                TagListState.Item.Tag(
                    tag = TagChipState(
                        id = 124,
                        name = "Personal",
                        color = 0xFF123456,
                        checked = false,
                        mode = TagChipState.TagSelectionMode.SINGLE,
                    ),
                ),
            ),
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
                    tag = TagChipState(
                        id = 123,
                        name = "Sleep",
                        color = 0xFF123456,
                        checked = true,
                        mode = TagChipState.TagSelectionMode.MULTI,
                    ),
                ),
                TagListState.Item.Tag(
                    tag = TagChipState(
                        id = 124,
                        name = "Personal",
                        color = 0xFF123456,
                        checked = false,
                        mode = TagChipState.TagSelectionMode.MULTI,
                    ),
                ),
            ),
        ),
    )
}