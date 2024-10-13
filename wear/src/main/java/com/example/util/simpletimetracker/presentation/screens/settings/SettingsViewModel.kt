/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.data.WearDataRepo
import com.example.util.simpletimetracker.domain.interactor.WearPrefsInteractor
import com.example.util.simpletimetracker.domain.model.WearSettings
import com.example.util.simpletimetracker.presentation.ui.components.SettingsListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsViewDataMapper: SettingsViewDataMapper,
    private val wearPrefsInteractor: WearPrefsInteractor,
    private val wearDataRepo: WearDataRepo,
) : ViewModel() {

    val state: StateFlow<SettingsListState> get() = _state.asStateFlow()
    private val _state: MutableStateFlow<SettingsListState> = MutableStateFlow(SettingsListState.Loading)

    private var isInitialized = false
    private var settings: WearSettings? = null

    fun init() {
        if (isInitialized) return
        viewModelScope.launch { loadData(true) }
        subscribeToDataUpdates()
        isInitialized = true
    }

    fun onRefresh() = viewModelScope.launch {
        loadData(forceReload = true)
    }

    fun onSettingClick(itemType: SettingsItemType) = viewModelScope.launch {
        when (itemType) {
            is SettingsItemType.AllowMultitasking -> {
                val settings = this@SettingsViewModel.settings ?: return@launch
                wearDataRepo.setSettings(
                    settings.copy(
                        allowMultitasking = !settings.allowMultitasking,
                    ),
                )
            }
            is SettingsItemType.ShowCompactList -> {
                val value = wearPrefsInteractor.getWearShowCompactList()
                wearPrefsInteractor.setWearShowCompactList(!value)
            }
        }
        loadData(forceReload = true)
    }

    private suspend fun loadData(forceReload: Boolean) {
        val showCompactList = wearPrefsInteractor.getWearShowCompactList()
        val settings = wearDataRepo.loadSettings(forceReload)

        when {
            settings.isFailure -> {
                showError()
            }
            else -> {
                this.settings = settings.getOrNull()
                _state.value = settingsViewDataMapper.mapContentState(
                    showCompactList = showCompactList,
                    wearSettings = this.settings ?: return,
                )
            }
        }
    }

    private fun showError() {
        _state.value = settingsViewDataMapper.mapErrorState()
    }

    private fun subscribeToDataUpdates() {
        viewModelScope.launch {
            wearDataRepo.dataUpdated.collect { loadData(forceReload = false) }
        }
    }
}