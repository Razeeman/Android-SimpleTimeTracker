package com.example.util.simpletimetracker.feature_settings.viewModel.delegate

import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.feature_settings.interactor.SettingsContributorsViewDataInteractor
import javax.inject.Inject

class SettingsContributorsViewModelDelegate @Inject constructor(
    private val settingsContributorsViewDataInteractor: SettingsContributorsViewDataInteractor,
) : SettingsDelegate, ViewModelDelegate() {

    override suspend fun getViewData(): SettingsDelegate.ViewData {
        return SettingsDelegate.ViewData(
            key = Companion,
            data = settingsContributorsViewDataInteractor.execute(),
        )
    }

    companion object : SettingsDelegate.Key
}