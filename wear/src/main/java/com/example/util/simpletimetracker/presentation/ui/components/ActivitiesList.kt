/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.ScalingLazyListScope
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.domain.model.WearActivityIcon
import com.example.util.simpletimetracker.presentation.ui.layout.ScaffoldedScrollingColumn
import com.example.util.simpletimetracker.utils.getString
import com.example.util.simpletimetracker.utils.orZero
import java.time.Instant
import java.util.UUID

sealed interface ActivitiesListState {
    object Loading : ActivitiesListState

    data class Error(
        @StringRes val messageResId: Int,
    ) : ActivitiesListState

    data class Empty(
        @StringRes val messageResId: Int,
    ) : ActivitiesListState

    data class Content(
        val isCompact: Boolean,
        val items: List<ActivityChipState>,
    ) : ActivitiesListState
}

@Composable
fun ActivitiesList(
    state: ActivitiesListState,
    onStart: (activityId: Long) -> Unit = {},
    onStop: (activityId: Long) -> Unit = {},
    onRefresh: () -> Unit = {},
    onOpenOnPhone: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
) {
    ScaffoldedScrollingColumn(
        startItemIndex = 1,
    ) {
        when (state) {
            is ActivitiesListState.Loading -> item {
                RenderLoading()
            }
            is ActivitiesListState.Error -> item {
                RenderError(state, onRefresh)
            }
            is ActivitiesListState.Empty -> item {
                RenderEmpty(state, onOpenOnPhone)
            }
            is ActivitiesListState.Content -> {
                renderContent(
                    state = state,
                    onStart = onStart,
                    onStop = onStop,
                    onSettingsClick = onSettingsClick,
                )
                item { RefreshButton(onRefresh) }
            }
        }
    }
}

@Composable
private fun RenderLoading() {
    CircularProgressIndicator(
        modifier = Modifier.width(64.dp),
    )
}

@Composable
private fun RenderError(
    state: ActivitiesListState.Error,
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
private fun RenderEmpty(
    state: ActivitiesListState.Empty,
    onOpenOnPhone: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = getString(stringResId = state.messageResId),
            modifier = Modifier.padding(8.dp),
            textAlign = TextAlign.Center,
        )
        OpenOnPhoneButton(onOpenOnPhone)
    }
}

private fun ScalingLazyListScope.renderContent(
    state: ActivitiesListState.Content,
    onStart: (activityId: Long) -> Unit,
    onStop: (activityId: Long) -> Unit,
    onSettingsClick: () -> Unit,
) {
    item {
        SettingsButton(onSettingsClick)
    }
    if (state.isCompact) {
        renderContentCompact(
            state = state,
            onStart = onStart,
            onStop = onStop,
        )
    } else {
        renderContentFull(
            state = state,
            onStart = onStart,
            onStop = onStop,
        )
    }
}

private fun ScalingLazyListScope.renderContentFull(
    state: ActivitiesListState.Content,
    onStart: (activityId: Long) -> Unit,
    onStop: (activityId: Long) -> Unit,
) {
    for (itemState in state.items) {
        item(key = itemState.id) {
            val isRunning = itemState.startedAt != null
            val onClick = remember(itemState) {
                {
                    if (isRunning) {
                        onStop(itemState.id)
                    } else {
                        onStart(itemState.id)
                    }
                }
            }
            ActivityChip(
                state = itemState,
                onClick = onClick,
            )
        }
    }
}

private fun ScalingLazyListScope.renderContentCompact(
    state: ActivitiesListState.Content,
    onStart: (activityId: Long) -> Unit,
    onStop: (activityId: Long) -> Unit,
) {
    state.items
        .withIndex()
        .groupBy { it.index / ACTIVITY_LIST_COMPACT_CHIP_COUNT }
        .map { it.value.map { part -> part.value } }
        .forEach { part ->
            item(key = part.firstOrNull()?.id.orZero()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CompactChipPlaceHolder(part.size)
                    part.forEach { itemState ->
                        val isRunning = itemState.startedAt != null
                        val onClick = remember(itemState) {
                            {
                                if (isRunning) {
                                    onStop(itemState.id)
                                } else {
                                    onStart(itemState.id)
                                }
                            }
                        }
                        ActivityChipCompact(
                            modifier = Modifier
                                .fillMaxSize()
                                .aspectRatio(1f)
                                .weight(1f),
                            state = ActivityChipCompatState(
                                id = itemState.id,
                                icon = itemState.icon,
                                color = itemState.color,
                                startedAt = itemState.startedAt,
                            ),
                            onClick = onClick,
                        )
                    }
                    CompactChipPlaceHolder(part.size)
                }
            }
        }
}

@Composable
private fun RowScope.CompactChipPlaceHolder(
    partSize: Int,
) {
    if (partSize < ACTIVITY_LIST_COMPACT_CHIP_COUNT) {
        val weight = if (partSize == 1) 1f else 0.5f
        Box(
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(1f)
                .weight(weight),
        )
    }
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun Loading() {
    ActivitiesList(
        state = ActivitiesListState.Loading,
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun Error() {
    ActivitiesList(
        state = ActivitiesListState.Error(R.string.wear_loading_error),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun NoActivities() {
    ActivitiesList(
        state = ActivitiesListState.Empty(R.string.record_types_empty),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun ContentFull() {
    val items = List(5) {
        ActivityChipState(
            id = UUID.randomUUID().hashCode().toLong(),
            name = "Sleep",
            icon = WearActivityIcon.Image(R.drawable.ic_hotel_24px),
            color = 0xFF0000FA,
            startedAt = Instant.now().toEpochMilli() - 36500000,
            tagString = "",
        )
    }
    ActivitiesList(
        state = ActivitiesListState.Content(
            isCompact = false,
            items = items,
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun ContentCompact() {
    val items = List(5) {
        ActivityChipState(
            id = UUID.randomUUID().hashCode().toLong(),
            name = "Sleep",
            icon = WearActivityIcon.Image(R.drawable.ic_hotel_24px),
            color = 0xFF0000FA,
            startedAt = Instant.now().toEpochMilli() - 36500000,
            tagString = "",
        )
    }
    ActivitiesList(
        state = ActivitiesListState.Content(
            isCompact = true,
            items = items,
        ),
    )
}