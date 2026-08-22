/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.features.activities.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyListScope
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.domain.model.WearActivityIcon
import com.example.util.simpletimetracker.features.activities.ui.ActivityChip
import com.example.util.simpletimetracker.features.activities.ui.ActivityChipCompact
import com.example.util.simpletimetracker.features.activities.ui.ActivityChipCompatState
import com.example.util.simpletimetracker.features.activities.ui.ActivityChipState
import com.example.util.simpletimetracker.features.activities.ui.ActivityChipType
import com.example.util.simpletimetracker.features.activities.ui.NavigationButton
import com.example.util.simpletimetracker.features.activities.ui.OpenOnPhoneButton
import com.example.util.simpletimetracker.presentation.layout.ScaffoldedScrollingColumn
import com.example.util.simpletimetracker.presentation.theme.ColorInactive
import com.example.util.simpletimetracker.presentation.ui.ACTIVITY_LIST_COMPACT_CHIP_COUNT
import com.example.util.simpletimetracker.presentation.ui.Divider
import com.example.util.simpletimetracker.presentation.ui.ErrorState
import com.example.util.simpletimetracker.presentation.ui.Hint
import com.example.util.simpletimetracker.presentation.ui.HintState
import com.example.util.simpletimetracker.presentation.ui.RefreshButton
import com.example.util.simpletimetracker.presentation.ui.RenderLoading
import com.example.util.simpletimetracker.presentation.ui.renderError
import com.example.util.simpletimetracker.utils.getString
import java.time.Instant
import java.util.UUID

sealed interface ActivitiesListState {

    data object Loading : ActivitiesListState

    data class Error(
        val error: ErrorState,
    ) : ActivitiesListState

    data class Empty(
        @StringRes val messageResId: Int,
    ) : ActivitiesListState

    data class Content(
        val hint: String,
        val isCompact: Boolean,
        val items: List<Item>,
    ) : ActivitiesListState {

        sealed interface Item {
            data object Divider : Item
            data class Button(val data: ActivityChipState) : Item
        }
    }
}

@Composable
fun ActivitiesList(
    state: ActivitiesListState,
    onItemClick: (item: ActivityChipState) -> Unit = {},
    onRefresh: () -> Unit = {},
    onOpenOnPhone: () -> Unit = {},
    onStatisticsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
) {
    ScaffoldedScrollingColumn {
        when (state) {
            is ActivitiesListState.Loading -> item {
                RenderLoading()
            }
            is ActivitiesListState.Error -> {
                renderError(
                    state = state.error,
                    onRefresh = onRefresh,
                )
            }
            is ActivitiesListState.Empty -> {
                renderEmpty(
                    state = state,
                    onOpenOnPhone = onOpenOnPhone,
                )
            }
            is ActivitiesListState.Content -> {
                renderContent(
                    state = state,
                    onItemClick = onItemClick,
                    onStatisticsClick = onStatisticsClick,
                    onSettingsClick = onSettingsClick,
                )
                item { RefreshButton(onRefresh) }
            }
        }
    }
}

private fun ScalingLazyListScope.renderEmpty(
    state: ActivitiesListState.Empty,
    onOpenOnPhone: () -> Unit,
) {
    item {
        Spacer(modifier = Modifier)
    }
    item {
        Text(
            text = getString(stringResId = state.messageResId),
            modifier = Modifier.padding(horizontal = 8.dp),
            textAlign = TextAlign.Center,
        )
    }
    item {
        OpenOnPhoneButton(onOpenOnPhone)
    }
}

private fun ScalingLazyListScope.renderContent(
    state: ActivitiesListState.Content,
    onItemClick: (item: ActivityChipState) -> Unit,
    onStatisticsClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    item {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            NavigationButton(
                drawableResId = R.drawable.wear_statistics,
                onClick = onStatisticsClick,
            )
            NavigationButton(
                drawableResId = R.drawable.wear_settings,
                onClick = onSettingsClick,
            )
        }
    }
    if (state.isCompact) {
        renderContentCompact(
            state = state,
            onItemClick = onItemClick,
        )
    } else {
        renderContentFull(
            state = state,
            onItemClick = onItemClick,
        )
    }
}

private fun ScalingLazyListScope.renderContentFull(
    state: ActivitiesListState.Content,
    onItemClick: (item: ActivityChipState) -> Unit,
) {
    if (state.hint.isNotEmpty()) {
        item {
            Hint(HintState(state.hint))
        }
    }
    fun renderItem(itemState: ActivityChipState) {
        item(key = itemState.uniqueId) {
            val onClick = remember(itemState) {
                { onItemClick(itemState) }
            }
            ActivityChip(
                state = itemState,
                onClick = onClick,
            )
        }
    }
    for (itemState in state.items) {
        when (itemState) {
            is ActivitiesListState.Content.Item.Divider -> {
                item { Divider() }
            }
            is ActivitiesListState.Content.Item.Button -> {
                renderItem(itemState.data)
            }
        }
    }
}

private fun ScalingLazyListScope.renderContentCompact(
    state: ActivitiesListState.Content,
    onItemClick: (item: ActivityChipState) -> Unit,
) {
    if (state.hint.isNotEmpty()) {
        item {
            Hint(HintState(state.hint))
        }
    }
    fun renderItems(
        items: List<ActivitiesListState.Content.Item.Button>,
    ) = items
        .map { it.data }
        .withIndex()
        .groupBy { it.index / ACTIVITY_LIST_COMPACT_CHIP_COUNT }
        .map { it.value.map { part -> part.value } }
        .forEach { part ->
            item(key = part.first().uniqueId) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CompactChipPlaceHolder(part.size)
                    part.forEach { itemState ->
                        val onClick = remember(itemState) {
                            { onItemClick(itemState) }
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
                                type = itemState.type,
                                timeHint = itemState.timeHint,
                                timeHint2 = itemState.timeHint2,
                                isLoading = itemState.isLoading,
                            ),
                            onClick = onClick,
                        )
                    }
                    CompactChipPlaceHolder(part.size)
                }
            }
        }

    val currentPack = mutableListOf<ActivitiesListState.Content.Item.Button>()
    for (itemsState in state.items) {
        when (itemsState) {
            is ActivitiesListState.Content.Item.Divider -> {
                renderItems(currentPack)
                currentPack.clear()
                item { Divider() }
            }
            is ActivitiesListState.Content.Item.Button -> {
                currentPack += itemsState
            }
        }
    }
    renderItems(currentPack)
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
        state = ActivitiesListState.Error(
            ErrorState(R.string.wear_loading_error),
        ),
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
            timeHint = if (it == 1) {
                ActivityChipState.TimeHint.Timer(
                    Instant.now().toEpochMilli() - 36500000,
                )
            } else {
                ActivityChipState.TimeHint.None
            },
        ).let {
            ActivitiesListState.Content.Item.Button(it)
        }
    }
    ActivitiesList(
        state = ActivitiesListState.Content(
            isCompact = false,
            hint = "",
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
            timeHint = if (it == 1) {
                ActivityChipState.TimeHint.Timer(
                    Instant.now().toEpochMilli() - 36500000,
                )
            } else {
                ActivityChipState.TimeHint.None
            },
        ).let {
            ActivitiesListState.Content.Item.Button(it)
        }
    }
    ActivitiesList(
        state = ActivitiesListState.Content(
            isCompact = true,
            hint = "",
            items = items,
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun ContentFullRetroactiveMode() {
    val items = List(5) {
        if (it == 0) {
            ActivityChipState(
                id = UUID.randomUUID().hashCode().toLong(),
                name = "Untracked",
                icon = WearActivityIcon.Image(R.drawable.app_unknown),
                color = ColorInactive.toArgb().toLong(),
                timeHint = ActivityChipState.TimeHint.Timer(
                    Instant.now().toEpochMilli() - 3650000,
                ),
            )
        } else {
            ActivityChipState(
                id = UUID.randomUUID().hashCode().toLong(),
                name = "Sleep",
                icon = WearActivityIcon.Image(R.drawable.ic_hotel_24px),
                color = 0xFF0000FA,
                timeHint = if (it == 1) {
                    ActivityChipState.TimeHint.Duration(
                        36500000,
                    )
                } else {
                    ActivityChipState.TimeHint.None
                },
                hint = if (it == 1) {
                    "Last"
                } else {
                    ""
                },
            )
        }.let {
            ActivitiesListState.Content.Item.Button(it)
        }
    }
    ActivitiesList(
        state = ActivitiesListState.Content(
            isCompact = false,
            hint = "Retroactive mode hint",
            items = items,
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun ContentCompactRetroactiveMode() {
    val items = List(5) {
        if (it == 0) {
            ActivityChipState(
                id = UUID.randomUUID().hashCode().toLong(),
                name = "Untracked",
                icon = WearActivityIcon.Image(R.drawable.app_unknown),
                color = ColorInactive.toArgb().toLong(),
                timeHint = ActivityChipState.TimeHint.Timer(
                    Instant.now().toEpochMilli() - 3650000,
                ),
            )
        } else {
            ActivityChipState(
                id = UUID.randomUUID().hashCode().toLong(),
                name = "Sleep",
                icon = WearActivityIcon.Image(R.drawable.ic_hotel_24px),
                color = 0xFF0000FA,
                timeHint = if (it == 1) {
                    ActivityChipState.TimeHint.Timer(
                        Instant.now().toEpochMilli() - 3650000,
                    )
                } else {
                    ActivityChipState.TimeHint.None
                },
                timeHint2 = if (it == 1) {
                    ActivityChipState.TimeHint.Duration(
                        36500000,
                    )
                } else {
                    ActivityChipState.TimeHint.None
                },
            )
        }.let {
            ActivitiesListState.Content.Item.Button(it)
        }
    }
    ActivitiesList(
        state = ActivitiesListState.Content(
            isCompact = true,
            hint = "Retroactive mode hint",
            items = items,
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun ContentFullWithSuggestions() {
    val items = List<ActivitiesListState.Content.Item>(5) {
        ActivityChipState(
            id = UUID.randomUUID().hashCode().toLong(),
            name = "Sleep",
            icon = WearActivityIcon.Image(R.drawable.ic_hotel_24px),
            color = 0xFF0000FA,
            type = if (it == 0) {
                ActivityChipType.Suggestion(isLast = true)
            } else {
                ActivityChipType.Base
            },
            timeHint = if (it == 1) {
                ActivityChipState.TimeHint.Timer(
                    Instant.now().toEpochMilli() - 36500000,
                )
            } else {
                ActivityChipState.TimeHint.None
            },
        ).let {
            ActivitiesListState.Content.Item.Button(it)
        }
    }.toMutableList().apply {
        add(
            index = 1,
            element = ActivitiesListState.Content.Item.Divider,
        )
    }
    ActivitiesList(
        state = ActivitiesListState.Content(
            isCompact = false,
            hint = "",
            items = items,
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun ContentCompactWithSuggestions() {
    val items = List<ActivitiesListState.Content.Item>(5) {
        ActivityChipState(
            id = UUID.randomUUID().hashCode().toLong(),
            name = "Sleep",
            icon = WearActivityIcon.Image(R.drawable.ic_hotel_24px),
            color = 0xFF0000FA,
            type = if (it == 0) {
                ActivityChipType.Suggestion(isLast = true)
            } else {
                ActivityChipType.Base
            },
            timeHint = if (it == 1) {
                ActivityChipState.TimeHint.Timer(
                    Instant.now().toEpochMilli() - 36500000,
                )
            } else {
                ActivityChipState.TimeHint.None
            },
        ).let {
            ActivitiesListState.Content.Item.Button(it)
        }
    }.toMutableList().apply {
        add(
            index = 1,
            element = ActivitiesListState.Content.Item.Divider,
        )
    }
    ActivitiesList(
        state = ActivitiesListState.Content(
            isCompact = true,
            hint = "",
            items = items,
        ),
    )
}