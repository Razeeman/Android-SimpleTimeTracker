package com.example.util.simpletimetracker.feature_settings.view

import com.example.util.simpletimetracker.feature_settings.viewModel.SettingsViewModel
import dagger.Subcomponent

@Subcomponent
interface SettingsComponent {

    fun inject(viewModel: SettingsViewModel)
}