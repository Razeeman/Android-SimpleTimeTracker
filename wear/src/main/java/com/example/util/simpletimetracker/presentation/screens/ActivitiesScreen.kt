/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.util.simpletimetracker.presentation.components.ActivitiesList
import com.example.util.simpletimetracker.presentation.components.CreditsButton
import com.example.util.simpletimetracker.presentation.screens.ActivitiesViewModel.Effect
import com.example.util.simpletimetracker.presentation.utils.collectEffects
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

@Composable
fun ActivitiesScreen(
    onRequestTagSelection: (activityId: Long) -> Unit,
    onRequestCredits: () -> Unit,
) {
    val viewModel = hiltViewModel<ActivitiesViewModel>()
    viewModel.init()
    val state = viewModel.state.collectAsState()

    viewModel.effects.collectEffects(key = viewModel) {
        when (it) {
            is Effect.OnRequestTagSelection -> onRequestTagSelection(it.activityId)
        }
    }

    ActivitiesList(
        activities = state.value.activities,
        currentActivities = state.value.currentActivities,
        onSelectActivity = viewModel::onSelectActivity,
        onEnableActivity = viewModel::startActivityWithoutTags,
        onDisableActivity = viewModel::stopActivity,
        onRefresh = viewModel::refresh,
        footer = { CreditsButton(onClick = onRequestCredits) },
    )
}
