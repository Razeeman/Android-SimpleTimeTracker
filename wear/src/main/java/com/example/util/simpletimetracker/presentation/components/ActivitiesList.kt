/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
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
import com.example.util.simpletimetracker.domain.WearActivityIcon
import com.example.util.simpletimetracker.presentation.layout.ScaffoldedScrollingColumn
import com.example.util.simpletimetracker.utils.getString

sealed interface ActivitiesListState {
    object Loading : ActivitiesListState

    data class Error(
        @StringRes val messageResId: Int,
    ) : ActivitiesListState

    data class Empty(
        @StringRes val messageResId: Int,
    ) : ActivitiesListState

    data class Content(
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
) {
    ScaffoldedScrollingColumn {
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
            painter = painterResource(R.drawable.connection_error),
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
private fun Preview() {
    val items = listOf(
        ActivityChipState(
            id = 1234,
            name = "Chores",
            icon = WearActivityIcon.Text("🧹"),
            color = 0xFFFA0000,
        ),
        ActivityChipState(
            id = 4321,
            name = "Sleep",
            icon = WearActivityIcon.Text("🛏️"),
            color = 0xFF0000FA,
            startedAt = 1708241427000L,
            tagString = "",
        ),
    )
    ActivitiesList(
        state = ActivitiesListState.Content(
            items = items,
        ),
    )
}