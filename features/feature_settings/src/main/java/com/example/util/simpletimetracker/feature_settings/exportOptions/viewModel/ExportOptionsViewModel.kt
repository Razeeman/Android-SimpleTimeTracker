package com.example.util.simpletimetracker.feature_settings.exportOptions.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.base.SingleLiveEvent
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.feature_settings.interactor.SettingsAdvancedOptionsUpdateInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExportOptionsViewModel @Inject constructor(
    private val settingsAdvancedOptionsUpdateInteractor: SettingsAdvancedOptionsUpdateInteractor,
) : BaseViewModel() {

    val dismiss: LiveData<Unit> =
        SingleLiveEvent<Unit>()

    init {
        viewModelScope.launch {
            settingsAdvancedOptionsUpdateInteractor.dismiss.collect { dismiss.set(Unit) }
        }
    }
}