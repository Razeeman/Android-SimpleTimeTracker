/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.ScalingLazyListScope
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.presentation.layout.ScaffoldedScrollingColumn
import com.example.util.simpletimetracker.utils.getString
import com.example.util.simpletimetracker.wear_api.WearActivity
import com.example.util.simpletimetracker.wear_api.WearCurrentActivity

sealed interface ActivitiesListState {
    object Loading : ActivitiesListState

    data class Error(
        @StringRes val messageResId: Int,
    ) : ActivitiesListState

    data class Empty(
        @StringRes val messageResId: Int,
    ) : ActivitiesListState

    data class Content(
        val activities: List<WearActivity>,
        val currentActivities: List<WearCurrentActivity>,
    ) : ActivitiesListState
}

@Composable
fun ActivitiesList(
    state: ActivitiesListState,
    onStart: (activity: WearActivity) -> Unit = {},
    onStop: (activity: WearActivity) -> Unit = {},
    onRefresh: () -> Unit = {},
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
                RenderEmpty(state, onRefresh)
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
    onRefresh: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = getString(stringResId = state.messageResId),
            modifier = Modifier.padding(8.dp),
            textAlign = TextAlign.Center,
        )
        RefreshButton(onRefresh)
    }
}

private fun ScalingLazyListScope.renderContent(
    state: ActivitiesListState.Content,
    onStart: (activity: WearActivity) -> Unit,
    onStop: (activity: WearActivity) -> Unit,
) {
    for (activity in state.activities) {
        val currentActivity = state.currentActivities
            .firstOrNull { it.id == activity.id }

        item(key = activity.id) {
            ActivityChip(
                activity = activity,
                startedAt = currentActivity?.startedAt,
                tags = currentActivity?.tags.orEmpty(),
                onClick = {
                    if (currentActivity != null) {
                        onStop(activity)
                    } else {
                        onStart(activity)
                    }
                },
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
    val activities = listOf(
        WearActivity(1234, "Chores", "🧹", 0xFFFA0000),
        WearActivity(4321, "Sleep", "🛏️", 0xFF0000FA),
    )
    val currents = listOf(
        WearCurrentActivity(id = 4321, startedAt = 1708241427000L, tags = emptyList()),
    )
    ActivitiesList(
        state = ActivitiesListState.Content(
            activities = activities,
            currentActivities = currents,
        ),
    )
}