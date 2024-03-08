/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.screens.activities

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.util.simpletimetracker.presentation.components.ActivitiesList
import com.example.util.simpletimetracker.presentation.screens.activities.ActivitiesViewModel.Effect
import com.example.util.simpletimetracker.utils.OnLifecycle
import com.example.util.simpletimetracker.utils.collectEffects

@Composable
fun ActivitiesScreen(
    onRequestTagSelection: (activityId: Long) -> Unit,
) {
    val viewModel = hiltViewModel<ActivitiesViewModel>()
    viewModel.init()
    val state by viewModel.state.collectAsState()

    viewModel.effects.collectEffects(key = viewModel) {
        when (it) {
            is Effect.OnRequestTagSelection -> onRequestTagSelection(it.activityId)
        }
    }

    OnLifecycle(onStart = viewModel::onRefresh)

    ActivitiesList(
        state = state,
        onStart = viewModel::tryStartActivity,
        onStop = viewModel::stopActivity,
        onRefresh = viewModel::onRefresh,
        onOpenOnPhone = viewModel::onOpenOnPhone,
    )
}
