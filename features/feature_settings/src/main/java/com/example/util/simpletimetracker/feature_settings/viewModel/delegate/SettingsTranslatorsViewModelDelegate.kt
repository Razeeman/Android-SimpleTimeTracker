package com.example.util.simpletimetracker.feature_settings.viewModel.delegate

import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_settings.interactor.SettingsTranslatorsViewDataInteractor
import javax.inject.Inject

class SettingsTranslatorsViewModelDelegate @Inject constructor(
    private val settingsTranslatorsViewDataInteractor: SettingsTranslatorsViewDataInteractor,
) : SettingsDelegate, ViewModelDelegate() {

    override suspend fun getViewData(): SettingsDelegate.ViewData {
        return SettingsDelegate.ViewData(
            key = Companion,
            data = settingsTranslatorsViewDataInteractor.execute(),
        )
    }

    companion object : SettingsDelegate.Key
}