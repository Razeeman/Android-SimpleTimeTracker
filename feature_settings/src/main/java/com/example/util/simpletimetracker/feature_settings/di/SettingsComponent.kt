package com.example.util.simpletimetracker.feature_settings.di

import com.example.util.simpletimetracker.feature_settings.view.SettingsFragment
import dagger.Subcomponent

@Subcomponent
interface SettingsComponent {

    fun inject(fragment: SettingsFragment)
}