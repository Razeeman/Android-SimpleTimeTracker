package com.example.util.simpletimetracker.feature_settings.viewModel.delegate

import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_settings.interactor.SettingsContributorsViewDataInteractor
import javax.inject.Inject

class SettingsContributorsViewModelDelegate @Inject constructor(
    private val settingsContributorsViewDataInteractor: SettingsContributorsViewDataInteractor,
) : ViewModelDelegate() {

    fun getViewData(): List<ViewHolderType> {
        return settingsContributorsViewDataInteractor.execute()
    }
}