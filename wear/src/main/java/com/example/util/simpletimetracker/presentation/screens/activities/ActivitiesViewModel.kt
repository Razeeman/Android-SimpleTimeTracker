/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.screens.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.data.WearDataRepo
import com.example.util.simpletimetracker.domain.CurrentActivitiesMediator
import com.example.util.simpletimetracker.domain.StartActivityMediator
import com.example.util.simpletimetracker.presentation.components.ActivitiesListState
import com.example.util.simpletimetracker.wear_api.WearActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivitiesViewModel @Inject constructor(
    private val wearDataRepo: WearDataRepo,
    private val startActivitiesMediator: StartActivityMediator,
    private val currentActivitiesMediator: CurrentActivitiesMediator,
) : ViewModel() {

    val state: MutableStateFlow<ActivitiesListState> = MutableStateFlow(ActivitiesListState.Loading)
    val effects = MutableSharedFlow<Effect>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private var isInitialized = false

    fun init() {
        if (isInitialized) return
        loadData()
        subscribeToDataUpdates()
        isInitialized = true
    }

    fun stopActivity(wearActivity: WearActivity) = viewModelScope.launch {
        val result = currentActivitiesMediator.stop(wearActivity.id)
        if (result.isFailure) showError()
    }

    fun tryStartActivity(wearActivity: WearActivity) = viewModelScope.launch {
        val result = startActivitiesMediator.requestStart(
            activity = wearActivity,
            onRequestTagSelection = {
                effects.emit(Effect.OnRequestTagSelection(wearActivity.id))
            },
        )
        if (result.isFailure) showError()
    }

    fun onRefresh() {
        loadData()
    }

    private fun loadData() = viewModelScope.launch {
        val activities = wearDataRepo.loadActivities()
        val currentActivities = wearDataRepo.loadCurrentActivities()

        when {
            activities.isFailure || currentActivities.isFailure -> {
                showError()
            }
            activities.getOrNull().isNullOrEmpty() -> {
                state.value = ActivitiesListState.Empty(R.string.record_types_empty)
            }
            else -> {
                state.value = ActivitiesListState.Content(
                    activities = activities.getOrNull().orEmpty(),
                    currentActivities = currentActivities.getOrNull().orEmpty(),
                )
            }
        }
    }

    private fun showError() {
        state.value = ActivitiesListState.Error(R.string.wear_loading_error)
    }

    private fun subscribeToDataUpdates() {
        viewModelScope.launch {
            wearDataRepo.dataUpdated.collect { loadData() }
        }
    }

    sealed interface Effect {
        data class OnRequestTagSelection(val activityId: Long) : Effect
    }
}