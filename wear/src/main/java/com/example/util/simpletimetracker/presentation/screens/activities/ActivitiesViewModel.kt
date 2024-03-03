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
import com.example.util.simpletimetracker.presentation.components.ActivitiesListState
import com.example.util.simpletimetracker.data.WearRPCClient
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
    private val rpc: WearRPCClient,
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
        isInitialized = true
    }

    fun startActivityWithoutTags(wearActivity: WearActivity) {
        Log.d("ActivitiesScreen", "Starting ${wearActivity.name} (#${wearActivity.id}) without tags")
        viewModelScope.launch {
            currentActivitiesMediator.start(wearActivity.id)
            loadData()
        }
    }

    fun stopActivity(wearActivity: WearActivity) {
        Log.d("ActivitiesScreen", "Stopping ${wearActivity.name} (#${wearActivity.id})")
        viewModelScope.launch {
            currentActivitiesMediator.stop(wearActivity.id)
            loadData()
        }
    }

    fun onSelectActivity(wearActivity: WearActivity) {
        viewModelScope.launch {
            startActivitiesMediator.requestStart(
                activity = wearActivity,
                onRequestStartActivity = ::startActivityWithoutTags,
                onRequestTagSelection = ::startActivityWithTags,
            )
        }
    }

    fun loadData() {
        viewModelScope.launch {
            val activities = rpc.queryActivities()
            val newState = if (activities.isEmpty()) {
                ActivitiesListState.Empty(R.string.no_activities)
            } else {
                ActivitiesListState.Content(
                    activities = activities,
                    currentActivities = rpc.queryCurrentActivities(),
                )
            }
            state.value = newState
        }
    }

    private fun startActivityWithTags(wearActivity: WearActivity) {
        viewModelScope.launch {
            Log.d("ActivitiesScreen", "Starting ${wearActivity.name} (#${wearActivity.id}) with tags")
            effects.emit(Effect.OnRequestTagSelection(wearActivity.id))
        }
    }

    sealed interface Effect {
        data class OnRequestTagSelection(val activityId: Long) : Effect
    }
}