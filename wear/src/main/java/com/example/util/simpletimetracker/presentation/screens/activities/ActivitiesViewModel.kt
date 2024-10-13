/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.screens.activities

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.complication.WearComplicationManager
import com.example.util.simpletimetracker.data.WearDataRepo
import com.example.util.simpletimetracker.domain.interactor.WearCheckNotificationsPermissionInteractor
import com.example.util.simpletimetracker.domain.interactor.WearPrefsInteractor
import com.example.util.simpletimetracker.domain.mediator.StartActivityMediator
import com.example.util.simpletimetracker.domain.model.WearRecordRepeatResult
import com.example.util.simpletimetracker.notification.WearNotificationManager
import com.example.util.simpletimetracker.presentation.ui.components.ActivitiesListState
import com.example.util.simpletimetracker.presentation.ui.components.ActivityChipState
import com.example.util.simpletimetracker.presentation.ui.components.ActivityChipType
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
    private val activitiesViewDataMapper: ActivitiesViewDataMapper,
    private val wearPrefsInteractor: WearPrefsInteractor,
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

    fun onItemClick(item: ActivityChipState) {
        when (item.type) {
            is ActivityChipType.Base -> {
                val isRunning = item.startedAt != null
                if (isRunning) {
                    stopActivity(item.id)
                } else {
                    tryStartActivity(item.id)
                }
            }
            is ActivityChipType.Repeat -> {
                repeatActivity()
            }
        }
    }

    private fun stopActivity(activityId: Long) = viewModelScope.launch {
        setLoading(activityId, isLoading = true)
        val result = startActivitiesMediator.stop(activityId)
        if (result.isFailure) showError()
    }

    private fun tryStartActivity(activityId: Long) {
        wearCheckNotificationsPermissionInteractor.execute(
            onEnabled = { startActivity(activityId) },
            onDisabled = { startActivity(activityId) },
        )
    }

    private fun repeatActivity() = viewModelScope.launch {
        val result = startActivitiesMediator.repeat()
        when (result.getOrNull()?.result) {
            is WearRecordRepeatResult.ActionResult.Started -> {
                // Do nothing
            }
            is WearRecordRepeatResult.ActionResult.NoPreviousFound ->
                showMessage(R.string.running_records_repeat_no_prev_record)
            is WearRecordRepeatResult.ActionResult.AlreadyTracking ->
                showMessage(R.string.running_records_repeat_already_tracking)
            null -> showError()
        }
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
            onProgressChanged = { isLoading ->
                setLoading(activityId, isLoading)
            },
        )
        if (result.isFailure) showError()
    }

    private fun setLoading(
        activityId: Long,
        isLoading: Boolean,
    ) {
        // Loading will be set to false when data will be received and update requested.
        val currentState = _state.value as? ActivitiesListState.Content
            ?: return
        val newItems = currentState.items.map {
            if (it.id == activityId) it.copy(isLoading = isLoading) else it
        }
        _state.value = currentState.copy(items = newItems)
    }

    private suspend fun loadData(forceReload: Boolean) {
        val activities = wearDataRepo.loadActivities(forceReload)
        val currentActivities = wearDataRepo.loadCurrentActivities(forceReload)
        val settings = wearDataRepo.loadSettings(forceReload)

        val loadError = activities.isFailure ||
            currentActivities.isFailure ||
            settings.isFailure

        when {
            loadError -> {
                showError()
            }
            activities.getOrNull().isNullOrEmpty() -> {
                _state.value = activitiesViewDataMapper.mapEmptyState()
            }
            else -> {
                _state.value = activitiesViewDataMapper.mapContentState(
                    activities = activities.getOrNull().orEmpty(),
                    currentActivities = currentActivities.getOrNull().orEmpty(),
                    settings = settings.getOrNull(),
                    showCompactList = wearPrefsInteractor.getWearShowCompactList(),
                )
            }
        }
    }

    private fun showError() {
        _state.value = activitiesViewDataMapper.mapErrorState()
    }

    private suspend fun showMessage(@StringRes textResId: Int) {
        _effects.emit(Effect.ShowMessage(textResId))
    }

    private fun subscribeToDataUpdates() {
        viewModelScope.launch {
            wearDataRepo.dataUpdated.collect { loadData(forceReload = false) }
        }
    }

    sealed interface Effect {
        data class OnRequestTagSelection(val activityId: Long) : Effect
        data class ShowMessage(val textResId: Int) : Effect
    }
}