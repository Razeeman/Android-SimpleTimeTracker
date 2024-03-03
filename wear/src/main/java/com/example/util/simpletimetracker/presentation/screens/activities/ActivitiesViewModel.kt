/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.screens.activities

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.data.WearDataRepo
import com.example.util.simpletimetracker.presentation.components.ActivitiesListState
import com.example.util.simpletimetracker.domain.CurrentActivitiesMediator
import com.example.util.simpletimetracker.domain.StartActivityMediator
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

    fun startActivityWithoutTags(wearActivity: WearActivity) = viewModelScope.launch {
        Log.d("ActivitiesScreen", "Starting ${wearActivity.name} (#${wearActivity.id}) without tags")
        currentActivitiesMediator.start(wearActivity.id)
    }

    fun stopActivity(wearActivity: WearActivity) = viewModelScope.launch {
        Log.d("ActivitiesScreen", "Stopping ${wearActivity.name} (#${wearActivity.id})")
        currentActivitiesMediator.stop(wearActivity.id)
    }

    fun onSelectActivity(wearActivity: WearActivity) = viewModelScope.launch {
        startActivitiesMediator.requestStart(
            activity = wearActivity,
            onRequestStartActivity = ::startActivityWithoutTags,
            onRequestTagSelection = ::startActivityWithTags,
        )
    }

    fun loadData() = viewModelScope.launch {
        val activities = wearDataRepo.loadActivities()
        val newState = if (activities.isEmpty()) {
            ActivitiesListState.Empty(R.string.no_activities)
        } else {
            ActivitiesListState.Content(
                activities = activities,
                currentActivities = wearDataRepo.loadCurrentActivities(),
            )
        }
        state.value = newState
    }

    private fun subscribeToDataUpdates() {
        viewModelScope.launch {
            wearDataRepo.currentActivitiesUpdated.collect { loadData() }
        }
        viewModelScope.launch {
            wearDataRepo.activitiesUpdated.collect { loadData() }
        }
    }

    private fun startActivityWithTags(wearActivity: WearActivity) = viewModelScope.launch {
        Log.d("ActivitiesScreen", "Starting ${wearActivity.name} (#${wearActivity.id}) with tags")
        effects.emit(Effect.OnRequestTagSelection(wearActivity.id))
    }

    sealed interface Effect {
        data class OnRequestTagSelection(val activityId: Long) : Effect
    }
}