package com.example.util.simpletimetracker.feature_settings.viewModel.delegate

import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_settings.interactor.SettingsTranslatorsViewDataInteractor
import javax.inject.Inject

class SettingsTranslatorsViewModelDelegate @Inject constructor(
    private val settingsTranslatorsViewDataInteractor: SettingsTranslatorsViewDataInteractor,
) : ViewModelDelegate() {

    fun getViewData(): List<ViewHolderType> {
        return settingsTranslatorsViewDataInteractor.execute()
    }
}