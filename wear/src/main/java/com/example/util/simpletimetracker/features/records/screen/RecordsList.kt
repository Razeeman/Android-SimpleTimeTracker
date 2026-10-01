/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.features.records.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyListScope
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.domain.model.WearActivityIcon
import com.example.util.simpletimetracker.features.records.ui.RecordChip
import com.example.util.simpletimetracker.features.records.ui.RecordChipState
import com.example.util.simpletimetracker.features.statistics.ui.StatisticsButtonsRow
import com.example.util.simpletimetracker.features.statistics.ui.StatisticsTitle
import com.example.util.simpletimetracker.presentation.layout.ScaffoldedScrollingColumn
import com.example.util.simpletimetracker.presentation.ui.ACTIVITY_RUNNING_VIEW_HEIGHT
import com.example.util.simpletimetracker.presentation.ui.ErrorState
import com.example.util.simpletimetracker.presentation.ui.RenderLoading
import com.example.util.simpletimetracker.presentation.ui.renderError
import com.example.util.simpletimetracker.utils.getCoercedFontScale
import com.example.util.simpletimetracker.utils.getString

sealed interface RecordsListState {
    data object Loading : RecordsListState

    data class Error(
        val error: ErrorState,
    ) : RecordsListState

    data class Empty(
        val title: String,
        @StringRes val messageResId: Int,
    ) : RecordsListState

    data class Content(
        val title: String,
        val items: List<RecordChipState>,
        val isLoading: Boolean,
    ) : RecordsListState
}

@Composable
fun RecordsList(
    state: RecordsListState,
    onRefresh: () -> Unit = {},
    onTitleClick: () -> Unit = {},
    onTitleLongClick: () -> Unit = {},
    onPrevClick: () -> Unit = {},
    onNextClick: () -> Unit = {},
) {
    Box {
        ScaffoldedScrollingColumn {
            when (state) {
                is RecordsListState.Loading -> item {
                    RenderLoading()
                }
                is RecordsListState.Error -> {
                    renderError(
                        state = state.error,
                        onRefresh = onRefresh,
                    )
                }
                is RecordsListState.Empty -> {
                    renderEmptyState(
                        state = state,
                        onTitleClick = onTitleClick,
                        onTitleLongClick = onTitleLongClick,
                    )
                }
                is RecordsListState.Content -> {
                    renderContent(
                        state = state,
                        onTitleClick = onTitleClick,
                        onTitleLongClick = onTitleLongClick,
                    )
                }
            }
        }

        val showControls = state is RecordsListState.Empty ||
            state is RecordsListState.Content
        if (showControls) {
            StatisticsButtonsRow(
                onPrevClick = onPrevClick,
                onNextClick = onNextClick,
            )
        }
    }
}

private fun ScalingLazyListScope.renderEmptyState(
    state: RecordsListState.Empty,
    onTitleClick: () -> Unit,
    onTitleLongClick: () -> Unit,
) {
    item {
        StatisticsTitle(
            title = state.title,
            onClick = onTitleClick,
            onLongClick = onTitleLongClick,
        )
    }
    item {
        val height = ACTIVITY_RUNNING_VIEW_HEIGHT *
            getCoercedFontScale()
        Box(
            modifier = Modifier.height(height.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = getString(state.messageResId),
                modifier = Modifier.padding(8.dp),
            )
        }
    }
}

private fun ScalingLazyListScope.renderContent(
    state: RecordsListState.Content,
    onTitleClick: () -> Unit,
    onTitleLongClick: () -> Unit,
) {
    item {
        StatisticsTitle(
            title = state.title,
            onClick = onTitleClick,
            onLongClick = onTitleLongClick,
        )
    }
    if (state.isLoading) {
        item {
            val height = ACTIVITY_RUNNING_VIEW_HEIGHT *
                getCoercedFontScale()
            Box(
                modifier = Modifier.height(height.dp),
                contentAlignment = Alignment.Center,
            ) {
                RenderLoading()
            }
        }
    } else {
        state.items.forEach { item ->
            item(key = item.key) {
                RecordChip(item)
            }
        }
    }
    // To avoid last item being cut off by prev next buttons.
    item {
        Spacer(Modifier)
    }
}

@Preview(device = WearDevices.LARGE_ROUND, fontScale = 1f)
@Composable
private fun Loading() {
    RecordsList(
        state = RecordsListState.Loading,
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun Error() {
    RecordsList(
        state = RecordsListState.Error(
            ErrorState(R.string.wear_loading_error),
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND, fontScale = 1f)
@Composable
private fun NoData() {
    RecordsList(
        state = RecordsListState.Empty(
            title = "Today",
            messageResId = R.string.no_data,
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND, fontScale = 1f)
@Composable
private fun Content() {
    RecordsList(
        state = RecordsListState.Content(
            title = "Tue, Mar 12",
            items = listOf(
                RecordChipState(
                    key = "1",
                    name = "Reading",
                    icon = WearActivityIcon.Text("📖"),
                    color = 0xFF455A64,
                    tags = "Home, Pages (24)",
                    time = "10:20 – 11:45",
                    duration = "1h 25m",
                    isRunning = false,
                    isUntracked = false,
                ),
                RecordChipState(
                    key = "2",
                    name = "Reading",
                    icon = WearActivityIcon.Text("📖"),
                    color = 0xFF455A64,
                    tags = "Home, Pages (24)",
                    time = "10:20 – 11:45",
                    duration = "1h 25m",
                    isRunning = false,
                    isUntracked = false,
                ),
            ),
            isLoading = false,
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND, fontScale = 1f)
@Composable
private fun ContentLoading() {
    RecordsList(
        state = RecordsListState.Content(
            title = "Tue, Mar 12",
            items = emptyList(),
            isLoading = true,
        ),
    )
}
