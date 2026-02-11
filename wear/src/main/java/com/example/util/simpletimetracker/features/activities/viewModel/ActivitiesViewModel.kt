/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.features.activities.viewModel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.BuildConfig
import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.complication.WearComplicationManager
import com.example.util.simpletimetracker.data.WearDataRepo
import com.example.util.simpletimetracker.domain.interactor.WearCheckNotificationsPermissionInteractor
import com.example.util.simpletimetracker.domain.interactor.WearPrefsInteractor
import com.example.util.simpletimetracker.domain.mediator.StartActivityMediator
import com.example.util.simpletimetracker.domain.model.WearRecordRepeatResult
import com.example.util.simpletimetracker.domain.model.WearSettings
import com.example.util.simpletimetracker.features.activities.mapper.ActivitiesViewDataMapper
import com.example.util.simpletimetracker.features.activities.screen.ActivitiesListState
import com.example.util.simpletimetracker.features.activities.ui.ActivityChipState
import com.example.util.simpletimetracker.features.activities.ui.ActivityChipType
import com.example.util.simpletimetracker.notification.WearNotificationManager
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
            is ActivityChipType.Base,
            is ActivityChipType.Suggestion,
            -> {
                if (item.isRunning) {
                    stopActivity(item.id)
                } else {
                    tryStartActivity(item.id)
                }
            }
            is ActivityChipType.Repeat -> {
                repeatActivity(item.id)
            }
            is ActivityChipType.Untracked -> {
                // Do nothing.
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

    private fun repeatActivity(itemId: Long) = viewModelScope.launch {
        setLoading(itemId, isLoading = true)
        val result = startActivitiesMediator.repeat()
        when (result.getOrNull()?.result) {
            is WearRecordRepeatResult.ActionResult.Started -> {
                // Do nothing
            }
            is WearRecordRepeatResult.ActionResult.NoPreviousFound -> {
                setLoading(itemId, isLoading = false)
                showMessage(R.string.running_records_repeat_no_prev_record)
            }
            is WearRecordRepeatResult.ActionResult.AlreadyTracking -> {
                setLoading(itemId, isLoading = false)
                showMessage(R.string.running_records_repeat_already_tracking)
            }
            null -> showError()
        }
    }

    fun onStart() {
        onRefresh()
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
            if (it !is ActivitiesListState.Content.Item.Button) return@map it
            if (it.data.id != activityId) return@map it
            val newData = it.data.copy(isLoading = isLoading)
            it.copy(data = newData)
        }
        _state.value = currentState.copy(items = newItems)
    }

    private suspend fun loadData(forceReload: Boolean) {
        val activities = wearDataRepo.loadActivities(forceReload)
        val currentState = wearDataRepo.loadCurrentActivities(forceReload)
        val settings = wearDataRepo.loadSettings(forceReload)

        val loadError = activities.isFailure ||
            currentState.isFailure ||
            settings.isFailure

        when {
            isUpdateRequired(settings) -> {
                showUpdateRequired()
            }
            loadError -> {
                showError()
            }
            activities.getOrNull().isNullOrEmpty() -> {
                _state.value = activitiesViewDataMapper.mapEmptyState()
            }
            else -> {
                _state.value = activitiesViewDataMapper.mapContentState(
                    activities = activities.getOrNull().orEmpty(),
                    currentActivities = currentState.getOrNull()?.currentActivities.orEmpty(),
                    suggestionIds = currentState.getOrNull()?.suggestionIds.orEmpty(),
                    lastRecords = currentState.getOrNull()?.lastRecords.orEmpty(),
                    settings = settings.getOrNull(),
                    showCompactList = wearPrefsInteractor.getWearShowCompactList(),
                )
            }
        }
    }

    private fun isUpdateRequired(
        settings: Result<WearSettings>,
    ): Boolean {
        // Either apiVersion field has changed or it is a connection error.
        if (settings.isFailure) return false
        val wearApiVersion = BuildConfig.WEAR_API_VERSION
        val phoneApiVersion = settings.getOrNull()?.apiVersion

        return wearApiVersion != phoneApiVersion
    }

    private fun showError() {
        _state.value = activitiesViewDataMapper.mapErrorState()
    }

    private fun showUpdateRequired() {
        _state.value = activitiesViewDataMapper.mapUpdateRequiredState()
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