/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.features.records.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.util.simpletimetracker.features.records.viewModel.RecordsViewModel
import com.example.util.simpletimetracker.features.records.viewModel.RecordsViewModel.Effect
import com.example.util.simpletimetracker.utils.collectEffects

@Composable
fun RecordsScreen(
    onOpenDatePicker: (Long) -> Unit,
) {
    val viewModel = hiltViewModel<RecordsViewModel>()
    viewModel.init()
    val state by viewModel.state.collectAsStateWithLifecycle()

    viewModel.effects.collectEffects(key = viewModel) {
        when (it) {
            is Effect.OnOpenDatePicker -> onOpenDatePicker(it.timestamp)
        }
    }

    RecordsList(
        state = state,
        onRefresh = viewModel::onRefresh,
        onTitleClick = viewModel::onTitleClick,
        onTitleLongClick = viewModel::onTitleLongClick,
        onPrevClick = viewModel::onPrevClick,
        onNextClick = viewModel::onNextClick,
    )
}
