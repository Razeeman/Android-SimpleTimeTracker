package com.example.util.simpletimetracker.feature_settings.exportOptions.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.base.SingleLiveEvent
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_settings.api.SettingsBlock
import com.example.util.simpletimetracker.feature_settings.interactor.SettingsAdvancedOptionsUpdateInteractor
import com.example.util.simpletimetracker.feature_settings.interactor.SettingsExportViewDataInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExportOptionsViewModel @Inject constructor(
    private val settingsExportViewDataInteractor: SettingsExportViewDataInteractor,
    private val settingsAdvancedOptionsUpdateInteractor: SettingsAdvancedOptionsUpdateInteractor,
) : BaseViewModel() {

    val content: LiveData<List<ViewHolderType>> by lazySuspend {
        loadContent()
    }
    val blockClicked: LiveData<SettingsBlock> =
        SingleLiveEvent<SettingsBlock>()
    val spinnerPositionSelected: LiveData<Pair<SettingsBlock, Int>> =
        SingleLiveEvent<Pair<SettingsBlock, Int>>()
    val dismiss: LiveData<Unit> =
        SingleLiveEvent<Unit>()

    init {
        viewModelScope.launch {
            settingsAdvancedOptionsUpdateInteractor.dataUpdated.collect { updateContent() }
        }
        viewModelScope.launch {
            settingsAdvancedOptionsUpdateInteractor.dismiss.collect { dismiss.set(Unit) }
        }
    }

    fun onBlockClicked(block: SettingsBlock) {
        blockClicked.set(block)
    }

    fun onSpinnerPositionSelected(block: SettingsBlock, position: Int) {
        spinnerPositionSelected.set(block to position)
    }

    private suspend fun updateContent() {
        content.set(loadContent())
    }

    private suspend fun loadContent(): List<ViewHolderType> {
        return settingsExportViewDataInteractor.executeAdvanced()
    }
}