/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.screens.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.data.WearDataRepo
import com.example.util.simpletimetracker.domain.CurrentActivitiesMediator
import com.example.util.simpletimetracker.domain.StartActivityMediator
import com.example.util.simpletimetracker.presentation.components.ActivitiesListState
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
    private val activitiesViewDataMapper: ActivitiesViewDataMapper,
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

    fun stopActivity(activityId: Long) = viewModelScope.launch {
        val result = currentActivitiesMediator.stop(activityId)
        if (result.isFailure) showError()
    }

    fun tryStartActivity(activityId: Long) = viewModelScope.launch {
        val result = startActivitiesMediator.requestStart(
            activityId = activityId,
            onRequestTagSelection = {
                effects.emit(Effect.OnRequestTagSelection(activityId))
            },
        )
        if (result.isFailure) showError()
    }

    fun onRefresh() {
        loadData()
    }

    fun onOpenOnPhone() = viewModelScope.launch {
        wearDataRepo.openAppPhone()
    }

    private fun loadData() = viewModelScope.launch {
        val activities = wearDataRepo.loadActivities()
        val currentActivities = wearDataRepo.loadCurrentActivities()

        when {
            activities.isFailure || currentActivities.isFailure -> {
                showError()
            }
            activities.getOrNull().isNullOrEmpty() -> {
                state.value = activitiesViewDataMapper.mapEmptyState()
            }
            else -> {
                state.value = activitiesViewDataMapper.mapContentState(
                    activities = activities.getOrNull().orEmpty(),
                    currentActivities = currentActivities.getOrNull().orEmpty(),
                )
            }
        }
    }

    private fun showError() {
        state.value = activitiesViewDataMapper.mapErrorState()
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