/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.screens.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.complication.WearComplicationManager
import com.example.util.simpletimetracker.data.WearDataRepo
import com.example.util.simpletimetracker.domain.CurrentActivitiesMediator
import com.example.util.simpletimetracker.domain.StartActivityMediator
import com.example.util.simpletimetracker.domain.WearCheckNotificationsPermissionInteractor
import com.example.util.simpletimetracker.notification.WearNotificationManager
import com.example.util.simpletimetracker.presentation.components.ActivitiesListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivitiesViewModel @Inject constructor(
    private val wearDataRepo: WearDataRepo,
    private val wearComplicationManager: WearComplicationManager,
    private val wearNotificationManager: WearNotificationManager,
    private val startActivitiesMediator: StartActivityMediator,
    private val currentActivitiesMediator: CurrentActivitiesMediator,
    private val activitiesViewDataMapper: ActivitiesViewDataMapper,
    private val wearCheckNotificationsPermissionInteractor: WearCheckNotificationsPermissionInteractor,
) : ViewModel() {

    val state: StateFlow<ActivitiesListState> get() = _state.asStateFlow()
    private val _state: MutableStateFlow<ActivitiesListState> = MutableStateFlow(ActivitiesListState.Loading)

    val effects: SharedFlow<Effect> get() = _effects.asSharedFlow()
    private val _effects = MutableSharedFlow<Effect>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private var isInitialized = false

    fun init() {
        if (isInitialized) return
        subscribeToDataUpdates()
        isInitialized = true
    }

    fun stopActivity(activityId: Long) = viewModelScope.launch {
        val result = currentActivitiesMediator.stop(activityId)
        if (result.isFailure) showError()
    }

    fun tryStartActivity(activityId: Long) {
        wearCheckNotificationsPermissionInteractor.execute(
            onEnabled = { startActivity(activityId) },
            onDisabled = { startActivity(activityId) },
        )
    }

    fun onRefresh() = viewModelScope.launch {
        loadData(forceReload = true)
        wearComplicationManager.updateComplications()
        wearNotificationManager.updateNotifications()
    }

    fun onOpenOnPhone() = viewModelScope.launch {
        wearDataRepo.openAppPhone()
    }

    private fun startActivity(activityId: Long) = viewModelScope.launch {
        val result = startActivitiesMediator.requestStart(
            activityId = activityId,
            onRequestTagSelection = {
                _effects.emit(Effect.OnRequestTagSelection(activityId))
            },
        )
        if (result.isFailure) showError()
    }

    private suspend fun loadData(forceReload: Boolean) {
        val activities = wearDataRepo.loadActivities(forceReload)
        val currentActivities = wearDataRepo.loadCurrentActivities(forceReload)

        when {
            activities.isFailure || currentActivities.isFailure -> {
                showError()
            }
            activities.getOrNull().isNullOrEmpty() -> {
                _state.value = activitiesViewDataMapper.mapEmptyState()
            }
            else -> {
                _state.value = activitiesViewDataMapper.mapContentState(
                    activities = activities.getOrNull().orEmpty(),
                    currentActivities = currentActivities.getOrNull().orEmpty(),
                )
            }
        }
    }

    private fun showError() {
        _state.value = activitiesViewDataMapper.mapErrorState()
    }

    private fun subscribeToDataUpdates() {
        viewModelScope.launch {
            wearDataRepo.dataUpdated.collect { loadData(forceReload = false) }
        }
    }

    sealed interface Effect {
        data class OnRequestTagSelection(val activityId: Long) : Effect
    }
}