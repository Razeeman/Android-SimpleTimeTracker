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
import androidx.compose.runtime.remember
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
import com.example.util.simpletimetracker.presentation.screens.settings.SettingsItemType
import com.example.util.simpletimetracker.utils.getString

sealed interface SettingsListState {
    object Loading : SettingsListState

    data class Error(
        @StringRes val messageResId: Int,
    ) : SettingsListState

    data class Content(
        val items: List<SettingsItem>,
    ) : SettingsListState
}

@Composable
fun SettingsList(
    state: SettingsListState,
    onRefresh: () -> Unit = {},
    onSettingClick: (SettingsItemType) -> Unit = {},
) {
    ScaffoldedScrollingColumn(
        startItemIndex = 0,
        spacedBy = 0.dp,
    ) {
        when (state) {
            is SettingsListState.Loading -> item {
                RenderLoading()
            }
            is SettingsListState.Error -> item {
                RenderError(state, onRefresh)
            }
            is SettingsListState.Content -> {
                renderContent(
                    state = state,
                    onSettingClick = onSettingClick,
                )
            }
        }
    }
}

// TODO move to outer element, replace in other screens.
// TODO same for error.
@Composable
private fun RenderLoading() {
    CircularProgressIndicator(
        modifier = Modifier.width(64.dp),
    )
}

@Composable
private fun RenderError(
    state: SettingsListState.Error,
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

private fun ScalingLazyListScope.renderContent(
    state: SettingsListState.Content,
    onSettingClick: (SettingsItemType) -> Unit,
) {
    for (item in state.items) {
        item {
            val onClick = remember(item) {
                { onSettingClick(item.type) }
            }
            when (item) {
                is SettingsItem.CheckBox -> {
                    SettingsCheckbox(
                        state = item,
                        onClick = onClick,
                    )
                }
                is SettingsItem.Hint -> {
                    SettingsHint(item)
                }
                is SettingsItem.Version -> {
                    SettingsVersion(item)
                }
            }
        }
    }
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun Loading() {
    SettingsList(
        state = SettingsListState.Loading,
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun Content() {
    val items = listOf(
        SettingsItem.CheckBox(
            type = SettingsItemType.ShowCompactList,
            text = "Setting",
            checked = true,
        ),
        SettingsItem.Hint(
            type = SettingsItemType.AllowMultitaskingHint,
            hint = "Hint",
        ),
        SettingsItem.Version(
            type = SettingsItemType.Version,
            text = "Version 1.43",
        ),
    )
    SettingsList(
        state = SettingsListState.Content(
            items = items,
        ),
    )
}

@Preview(
    device = WearDevices.LARGE_ROUND,
    showSystemUi = true,
    fontScale = 1.5f,
)
@Composable
private fun ContentLong() {
    val items = listOf(
        SettingsItem.CheckBox(
            type = SettingsItemType.ShowCompactList,
            text = "Setting Setting Setting Setting Setting",
            checked = true,
        ),
        SettingsItem.Hint(
            type = SettingsItemType.AllowMultitaskingHint,
            hint = "Hint Hint Hint Hint Hint Hint Hint Hint Hint Hint Hint Hint Hint Hint Hint Hint Hint Hint Hint Hint Hint Hint Hint Hint Hint Hint Hint Hint Hint Hint ",
        ),
        SettingsItem.Version(
            type = SettingsItemType.Version,
            text = "Version 1.43",
        ),
    )
    SettingsList(
        state = SettingsListState.Content(
            items = items,
        ),
    )
}